package com.mivoto.service.voting;

import com.mivoto.controller.dto.CastVoteRequest;
import com.mivoto.controller.dto.CastVoteResponse;
import com.mivoto.controller.dto.CastVoteSelection;
import com.mivoto.controller.dto.TallyEntry;
import com.mivoto.controller.dto.TallyResponse;
import com.mivoto.controller.dto.BallotResultResponse;
import com.mivoto.controller.dto.VerifyReceiptResponse;
import com.mivoto.model.Ballot;
import com.mivoto.model.BallotResult;
import com.mivoto.model.Candidate;
import com.mivoto.model.EligibilityStatus;
import com.mivoto.model.VoteRecord;
import com.mivoto.repository.CandidateRepository;
import com.mivoto.repository.BallotResultRepository;
import com.mivoto.repository.VoteRecordRepository;
import com.mivoto.repository.VoterEligibilityRepository;
import com.mivoto.infrastructure.blockchain.VoteContractService;
import com.mivoto.service.audit.AuditService;
import com.mivoto.service.eligibility.EligibilityService;
import com.mivoto.support.EligibilityTokenCodec.DecodedToken;
import com.mivoto.support.HashingService;
import com.mivoto.support.ResourceNotFoundException;
import com.mivoto.support.VotingException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

@Service
public class VotingService {

  private final VoteRecordRepository voteRecordRepository;
  private final VoterEligibilityRepository eligibilityRepository;
  private final EligibilityService eligibilityService;
  private final VoteContractService voteContractService;
  private final HashingService hashingService;
  private final AuditService auditService;
  private final BallotService ballotService;
  private final CandidateRepository candidateRepository;
  private final BallotResultRepository ballotResultRepository;
  private final Clock clock;

  public VotingService(VoteRecordRepository voteRecordRepository,
      VoterEligibilityRepository eligibilityRepository,
      EligibilityService eligibilityService,
      VoteContractService voteContractService,
      HashingService hashingService,
      AuditService auditService,
      BallotService ballotService,
      CandidateRepository candidateRepository,
      BallotResultRepository ballotResultRepository,
      Clock clock) {
    this.voteRecordRepository = Objects.requireNonNull(voteRecordRepository);
    this.eligibilityRepository = Objects.requireNonNull(eligibilityRepository);
    this.eligibilityService = Objects.requireNonNull(eligibilityService);
    this.voteContractService = Objects.requireNonNull(voteContractService);
    this.hashingService = Objects.requireNonNull(hashingService);
    this.auditService = Objects.requireNonNull(auditService);
    this.ballotService = Objects.requireNonNull(ballotService);
    this.candidateRepository = Objects.requireNonNull(candidateRepository);
    this.ballotResultRepository = Objects.requireNonNull(ballotResultRepository);
    this.clock = Objects.requireNonNull(clock);
  }

  public CastVoteResponse castVote(CastVoteRequest request) {
    Ballot ballot = ballotService.requireOpenBallot(request.ballotId());
    DecodedToken decoded = eligibilityService.decodeToken(request.eligibilityToken());

    String tokenHash = hashingService.hashToken(decoded.token(), decoded.salt());
    var eligibility = eligibilityRepository.findByTokenHash(tokenHash)
        .orElseThrow(() -> new VotingException("Eligibility token not recognized"));
    if (eligibility.status() != EligibilityStatus.ACTIVE) {
      throw new VotingException("Eligibility token not active");
    }
    if (eligibility.walletAddress() == null || eligibility.walletAddress().isBlank()) {
      throw new VotingException("Eligibility missing wallet address");
    }
    if (voteRecordRepository.existsByBallotIdAndSubjectHash(ballot.id(), eligibility.subjectHash())) {
      throw new VotingException("Subject already voted");
    }

    CastVoteSelection selection = Objects.requireNonNull(request.selection(), "Vote selection required");
    validateInstitution(selection, ballot);
    List<String> canonicalCandidates = canonicalizeCandidates(ballot, selection);
    assertCandidatesExist(canonicalCandidates, ballot.institutionId());

    Map<String, Object> votePayload = new LinkedHashMap<>();
    votePayload.put("institutionId", ballot.institutionId());
    votePayload.put("candidateIds", canonicalCandidates);

    String voteHash = hashingService.hashVotePayload(ballot.id(), votePayload);
    Instant now = Instant.now(clock);
    String receipt = hashingService.deriveReceipt(ballot.id(), voteHash, now);

    long ballotNumericId = parseBallotId(ballot.id());
    CompletableFuture<TransactionReceipt> future = voteContractService.castVote(
        ballotNumericId,
        tokenHash,
        voteHash,
        receipt);

    TransactionReceipt receiptOnChain;
    try {
      receiptOnChain = future.join();
    } catch (Exception e) {
      Throwable cause = e.getCause() != null ? e.getCause() : e;
      throw new VotingException("Blockchain submission failed", cause);
    }

    eligibilityRepository.markConsumed(tokenHash);

    String sbtTokenId = voteContractService.extractSbtTokenId(receiptOnChain, receipt)
        .orElse(null);

    auditService.record("voting-service", "VOTE_CAST", Map.of(
        "ballotId", ballot.id(),
        "receipt", receipt,
        "txHash", receiptOnChain.getTransactionHash(),
        "walletAddress", eligibility.walletAddress(),
        "sbtTokenId", sbtTokenId
    ));

    VoteRecord completed = new VoteRecord(
        UUID.randomUUID().toString(),
        ballot.id(),
        ballot.institutionId(),
        canonicalCandidates,
        voteHash,
        tokenHash,
        eligibility.subjectHash(),
        receipt,
        receiptOnChain.getTransactionHash(),
        sbtTokenId,
        now
    );
    voteRecordRepository.save(completed);

    return new CastVoteResponse(receipt, receiptOnChain.getTransactionHash(), sbtTokenId);
  }

  public VerifyReceiptResponse verifyReceipt(String receipt) {
    Optional<VoteRecord> voteRecord = voteRecordRepository.findByReceipt(receipt);
    boolean offChain = voteRecord.isPresent();
    boolean onChain = voteContractService.isReceiptRegistered(receipt);
    String ballotId = voteRecord.map(VoteRecord::ballotId).orElse("unknown");
    String txHash = voteRecord.map(VoteRecord::txHash).orElse(null);
    return new VerifyReceiptResponse(receipt, ballotId, onChain, offChain, txHash);
  }

  public TallyResponse tally(String ballotId) {
    Ballot ballot = ballotService.findBallot(ballotId)
        .orElseThrow(() -> new ResourceNotFoundException("Ballot not found: " + ballotId));
    Map<String, Long> normalized = normalizedCounts(ballot, voteRecordRepository.tallyByBallot(ballotId));
    List<TallyEntry> results = toEntries(ballot, normalized);
    return new TallyResponse(ballotId, results, Instant.now(clock));
  }

  public BallotResultResponse getFinalResult(String ballotId) {
    Ballot ballot = ballotService.findBallot(ballotId)
        .orElseThrow(() -> new ResourceNotFoundException("Ballot not found: " + ballotId));
    BallotResult result = ballotResultRepository.findByBallotId(ballotId)
        .orElseThrow(() -> new ResourceNotFoundException("Ballot result not available for " + ballotId));
    return toResultResponse(ballot, result, true);
  }

  public BallotResultResponse finalizeBallot(String ballotId) {
    Ballot ballot = ballotService.requireClosedBallot(ballotId);
    Optional<BallotResult> existing = ballotResultRepository.findByBallotId(ballotId);
    if (existing.isPresent()) {
      return toResultResponse(ballot, existing.get(), true);
    }

    Map<String, Long> normalized = normalizedCounts(ballot, voteRecordRepository.tallyByBallot(ballotId));
    Instant computedAt = Instant.now(clock);
    String checksum = hashingService.hashTally(ballotId, normalized, computedAt);
    BallotResult result = new BallotResult(
        UUID.randomUUID().toString(),
        ballot.id(),
        ballot.institutionId(),
        normalized,
        computedAt,
        checksum
    );
    ballotResultRepository.save(result);

    auditService.record("voting-service", "BALLOT_FINALIZED", Map.of(
        "ballotId", ballot.id(),
        "checksum", checksum
    ));

    return toResultResponse(ballot, result, true);
  }

  private Map<String, Long> normalizedCounts(Ballot ballot, Map<String, Long> counts) {
    Map<String, Long> normalized = ballotService.emptyTally(ballot);
    counts.forEach((candidateId, value) -> normalized.merge(candidateId, value, Long::sum));
    return normalized;
  }

  private List<TallyEntry> toEntries(Ballot ballot, Map<String, Long> normalized) {
    List<TallyEntry> results = new ArrayList<>();
    for (String candidateId : ballot.candidateIds()) {
      long votes = normalized.getOrDefault(candidateId, 0L);
      Candidate candidate = candidateRepository.findById(candidateId).orElse(null);
      results.add(new TallyEntry(
          candidateId,
          votes,
          candidate != null ? candidate.displayName() : candidateId,
          candidate != null ? candidate.listName() : null,
          candidate != null ? candidate.institutionId() : ballot.institutionId()
      ));
    }
    return results;
  }

  private BallotResultResponse toResultResponse(Ballot ballot, BallotResult result, boolean finalResult) {
    Map<String, Long> normalized = normalizedCounts(ballot, result.candidateVotes());
    return new BallotResultResponse(
        ballot.id(),
        toEntries(ballot, normalized),
        result.computedAt(),
        result.checksum(),
        finalResult
    );
  }

  private long parseBallotId(String ballotId) {
    try {
      return Long.parseLong(ballotId);
    } catch (NumberFormatException e) {
      throw new VotingException("Ballot id must be numeric for smart contract invocation", e);
    }
  }

  private void validateInstitution(CastVoteSelection selection, Ballot ballot) {
    if (!ballot.institutionId().equals(selection.institutionId())) {
      throw new VotingException("Institution mismatch for ballot");
    }
  }

  private List<String> canonicalizeCandidates(Ballot ballot, CastVoteSelection selection) {
    if (selection.candidateIds() == null || selection.candidateIds().isEmpty()) {
      throw new VotingException("At least one candidate must be selected");
    }
    List<String> distinct = selection.candidateIds().stream()
        .filter(Objects::nonNull)
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .distinct()
        .collect(Collectors.toList());
    if (!ballot.allowMultipleSelection() && distinct.size() > 1) {
      throw new VotingException("Ballot does not allow selecting multiple candidates");
    }
    for (String candidateId : distinct) {
      if (!ballot.candidateIds().contains(candidateId)) {
        throw new VotingException("Candidate " + candidateId + " is not part of ballot " + ballot.id());
      }
    }
    return ballot.candidateIds().stream()
        .filter(distinct::contains)
        .collect(Collectors.toList());
  }

  private void assertCandidatesExist(List<String> candidateIds, String institutionId) {
    for (String candidateId : candidateIds) {
      Candidate candidate = candidateRepository.findById(candidateId)
          .orElseThrow(() -> new VotingException("Candidate not found: " + candidateId));
      if (!institutionId.equals(candidate.institutionId())) {
        throw new VotingException("Candidate " + candidateId + " does not belong to institution " + institutionId);
      }
      if (!candidate.active()) {
        throw new VotingException("Candidate " + candidateId + " is not active");
      }
    }
  }
}
