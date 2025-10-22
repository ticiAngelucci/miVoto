package com.mivoto.service.voting;

import com.mivoto.controller.dto.CastVoteRequest;
import com.mivoto.controller.dto.CastVoteResponse;
import com.mivoto.controller.dto.TallyResponse;
import com.mivoto.controller.dto.VerifyReceiptResponse;
import com.mivoto.model.Ballot;
import com.mivoto.model.EligibilityStatus;
import com.mivoto.model.VoteRecord;
import com.mivoto.repository.VoteRecordRepository;
import com.mivoto.repository.VoterEligibilityRepository;
import com.mivoto.infrastructure.blockchain.VoteContractService;
import com.mivoto.service.audit.AuditService;
import com.mivoto.service.eligibility.EligibilityService;
import com.mivoto.support.EligibilityTokenCodec.DecodedToken;
import com.mivoto.support.HashingService;
import com.mivoto.support.VotingException;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
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
  private final Clock clock;

  public VotingService(VoteRecordRepository voteRecordRepository,
      VoterEligibilityRepository eligibilityRepository,
      EligibilityService eligibilityService,
      VoteContractService voteContractService,
      HashingService hashingService,
      AuditService auditService,
      BallotService ballotService,
      Clock clock) {
    this.voteRecordRepository = Objects.requireNonNull(voteRecordRepository);
    this.eligibilityRepository = Objects.requireNonNull(eligibilityRepository);
    this.eligibilityService = Objects.requireNonNull(eligibilityService);
    this.voteContractService = Objects.requireNonNull(voteContractService);
    this.hashingService = Objects.requireNonNull(hashingService);
    this.auditService = Objects.requireNonNull(auditService);
    this.ballotService = Objects.requireNonNull(ballotService);
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

    String voteHash = hashingService.hashVotePayload(ballot.id(), request.votePayload());
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

    auditService.record("voting-service", "VOTE_CAST", Map.of(
        "ballotId", ballot.id(),
        "receipt", receipt,
        "txHash", receiptOnChain.getTransactionHash()
    ));

    VoteRecord completed = new VoteRecord(
        UUID.randomUUID().toString(),
        ballot.id(),
        voteHash,
        tokenHash,
        receipt,
        receiptOnChain.getTransactionHash(),
        now
    );
    voteRecordRepository.save(completed);

    return new CastVoteResponse(receipt, receiptOnChain.getTransactionHash());
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
    Map<String, Long> counts = voteRecordRepository.tallyByBallot(ballotId);
    if (counts.isEmpty()) {
      counts = ballotService.emptyTally();
    }
    return new TallyResponse(ballotId, counts);
  }

  private long parseBallotId(String ballotId) {
    try {
      return Long.parseLong(ballotId);
    } catch (NumberFormatException e) {
      throw new VotingException("Ballot id must be numeric for smart contract invocation", e);
    }
  }
}
