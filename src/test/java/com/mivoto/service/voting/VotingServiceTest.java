package com.mivoto.service.voting;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.mivoto.controller.dto.CastVoteRequest;
import com.mivoto.controller.dto.CastVoteSelection;
import com.mivoto.controller.dto.TallyResponse;
import com.mivoto.model.Ballot;
import com.mivoto.model.BallotResult;
import com.mivoto.model.Candidate;
import com.mivoto.model.EligibilityStatus;
import com.mivoto.model.VoteRecord;
import com.mivoto.model.VoterEligibility;
import com.mivoto.repository.BallotResultRepository;
import com.mivoto.repository.CandidateRepository;
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
import java.time.ZoneOffset;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

@ExtendWith(MockitoExtension.class)
class VotingServiceTest {

  @Mock
  private VoteRecordRepository voteRecordRepository;
  @Mock
  private VoterEligibilityRepository voterEligibilityRepository;
  @Mock
  private EligibilityService eligibilityService;
  @Mock
  private VoteContractService voteContractService;
  @Mock
  private HashingService hashingService;
  @Mock
  private AuditService auditService;
  @Mock
  private BallotService ballotService;
  @Mock
  private CandidateRepository candidateRepository;
  @Mock
  private BallotResultRepository ballotResultRepository;

  private Clock clock;
  private VotingService votingService;

  @BeforeEach
  void setUp() {
    clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneOffset.UTC);
    votingService = new VotingService(
        voteRecordRepository,
        voterEligibilityRepository,
        eligibilityService,
        voteContractService,
        hashingService,
        auditService,
        ballotService,
        candidateRepository,
        ballotResultRepository,
        clock
    );
  }

  @Test
  void castVotePersistsNormalizedRecordAndReturnsReceipt() {
    Ballot ballot = new Ballot(
        "1",
        "inst-1",
        "Elección demo",
        List.of("cand-2", "cand-1"),
        Instant.now(clock).minusSeconds(60),
        Instant.now(clock).plusSeconds(3600),
        true
    );

    when(ballotService.requireOpenBallot("1")).thenReturn(ballot);
    DecodedToken decoded = new DecodedToken("raw-token", new byte[]{1, 2, 3}, Instant.now(clock).plusSeconds(600));
    when(eligibilityService.decodeToken("elig-token")).thenReturn(decoded);
    when(hashingService.hashToken(eq("raw-token"), any())).thenReturn("token-hash");
    VoterEligibility eligibility = new VoterEligibility(
        UUID.randomUUID().toString(),
        "subject-hash",
        Instant.now(clock),
        Instant.now(clock).plusSeconds(600),
        "token-hash",
        EligibilityStatus.ACTIVE,
        "mi-arg"
    );
    when(voterEligibilityRepository.findByTokenHash("token-hash")).thenReturn(Optional.of(eligibility));

    ArgumentCaptor<Map<String, Object>> payloadCaptor = ArgumentCaptor.forClass(Map.class);
    when(hashingService.hashVotePayload(eq("1"), payloadCaptor.capture())).thenReturn("vote-hash");
    when(hashingService.deriveReceipt(eq("1"), eq("vote-hash"), any())).thenReturn("receipt-123");

    TransactionReceipt receipt = new TransactionReceipt();
    receipt.setTransactionHash("0xtx");
    when(voteContractService.castVote(eq(1L), eq("token-hash"), eq("vote-hash"), eq("receipt-123")))
        .thenReturn(CompletableFuture.completedFuture(receipt));

    doNothing().when(voterEligibilityRepository).markConsumed("token-hash");
    when(candidateRepository.findById("cand-1"))
        .thenReturn(Optional.of(new Candidate("cand-1", "inst-1", "Lista Uno", null, null, true, Instant.now(clock), Instant.now(clock))));
    when(candidateRepository.findById("cand-2"))
        .thenReturn(Optional.of(new Candidate("cand-2", "inst-1", "Lista Dos", null, null, true, Instant.now(clock), Instant.now(clock))));
    ArgumentCaptor<VoteRecord> recordCaptor = ArgumentCaptor.forClass(VoteRecord.class);
    when(voteRecordRepository.save(recordCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

    CastVoteRequest request = new CastVoteRequest(
        "1",
        "elig-token",
        new CastVoteSelection("inst-1", List.of("cand-1", "cand-1", "cand-2"))
    );

    var response = votingService.castVote(request);

    assertThat(response.receipt()).isEqualTo("receipt-123");
    assertThat(response.txHash()).isEqualTo("0xtx");

    Map<String, Object> payload = payloadCaptor.getValue();
    assertThat(payload).containsEntry("institutionId", "inst-1");
    assertThat(payload).containsEntry("candidateIds", List.of("cand-2", "cand-1"));

    VoteRecord persisted = recordCaptor.getValue();
    assertThat(persisted.ballotId()).isEqualTo("1");
    assertThat(persisted.institutionId()).isEqualTo("inst-1");
    assertThat(persisted.candidateIds()).containsExactly("cand-2", "cand-1");
    verify(voterEligibilityRepository).markConsumed("token-hash");
  }

  @Test
  void castVoteRejectsCandidateOutsideBallot() {
    Ballot ballot = new Ballot(
        "1",
        "inst-1",
        "Elección demo",
        List.of("cand-1"),
        Instant.now(clock).minusSeconds(60),
        Instant.now(clock).plusSeconds(3600),
        false
    );
    when(ballotService.requireOpenBallot("1")).thenReturn(ballot);
    DecodedToken decoded = new DecodedToken("raw-token", new byte[]{1}, Instant.now(clock).plusSeconds(600));
    when(eligibilityService.decodeToken("elig-token")).thenReturn(decoded);
    when(hashingService.hashToken(eq("raw-token"), any())).thenReturn("token-hash");
    VoterEligibility eligibility = new VoterEligibility(
        UUID.randomUUID().toString(),
        "subject-hash",
        Instant.now(clock),
        Instant.now(clock).plusSeconds(600),
        "token-hash",
        EligibilityStatus.ACTIVE,
        "mi-arg"
    );
    when(voterEligibilityRepository.findByTokenHash("token-hash")).thenReturn(Optional.of(eligibility));

    CastVoteRequest request = new CastVoteRequest(
        "1",
        "elig-token",
        new CastVoteSelection("inst-1", List.of("cand-2"))
    );

    assertThatThrownBy(() -> votingService.castVote(request))
        .isInstanceOf(VotingException.class)
        .hasMessageContaining("Candidate cand-2 is not part of ballot");
  }

  @Test
  void tallyIncludesCandidatesFromBallotWithZeroCounts() {
    Ballot ballot = new Ballot(
        "1",
        "inst-1",
        "Elección demo",
        List.of("cand-1", "cand-2"),
        Instant.now(clock).minusSeconds(60),
        Instant.now(clock).plusSeconds(3600),
        true
    );
    when(ballotService.findBallot("1")).thenReturn(Optional.of(ballot));
    when(ballotService.emptyTally(ballot)).thenReturn(new java.util.LinkedHashMap<>(Map.of("cand-1", 0L, "cand-2", 0L)));
    when(voteRecordRepository.tallyByBallot("1")).thenReturn(Map.of("cand-1", 3L));
    when(candidateRepository.findById("cand-1"))
        .thenReturn(Optional.of(new Candidate("cand-1", "inst-1", "Lista Uno", null, null, true, Instant.now(clock), Instant.now(clock))));
    when(candidateRepository.findById("cand-2"))
        .thenReturn(Optional.of(new Candidate("cand-2", "inst-1", "Lista Dos", null, null, true, Instant.now(clock), Instant.now(clock))));

    TallyResponse response = votingService.tally("1");

    assertThat(response.ballotId()).isEqualTo("1");
    assertThat(response.results()).hasSize(2);
    assertThat(response.results().get(0).candidateId()).isEqualTo("cand-1");
    assertThat(response.results().get(0).votes()).isEqualTo(3L);
    assertThat(response.results().get(1).candidateId()).isEqualTo("cand-2");
    assertThat(response.results().get(1).votes()).isEqualTo(0L);
    assertThat(response.computedAt()).isEqualTo(Instant.now(clock));
  }

  @Test
  void tallyThrowsWhenBallotMissing() {
    when(ballotService.findBallot("missing")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> votingService.tally("missing"))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Ballot not found");
  }

  @Test
  void finalizeBallotCreatesSnapshotAndChecksum() {
    Ballot ballot = new Ballot(
        "1",
        "inst-1",
        "Elección demo",
        List.of("cand-1", "cand-2"),
        Instant.now(clock).minusSeconds(7200),
        Instant.now(clock).minusSeconds(60),
        true
    );
    when(ballotService.requireClosedBallot("1")).thenReturn(ballot);
    when(ballotResultRepository.findByBallotId("1")).thenReturn(Optional.empty());
    when(ballotService.emptyTally(ballot)).thenAnswer(invocation -> {
      Map<String, Long> base = new LinkedHashMap<>();
      base.put("cand-1", 0L);
      base.put("cand-2", 0L);
      return base;
    });
    when(voteRecordRepository.tallyByBallot("1")).thenReturn(Map.of("cand-1", 5L));
    when(hashingService.hashTally(eq("1"), anyMap(), any())).thenReturn("checksum-1");
    when(auditService.record(anyString(), anyString(), anyMap())).thenReturn(null);
    when(candidateRepository.findById("cand-1"))
        .thenReturn(Optional.of(new Candidate("cand-1", "inst-1", "Lista Uno", null, null, true, Instant.now(clock), Instant.now(clock))));
    when(candidateRepository.findById("cand-2"))
        .thenReturn(Optional.of(new Candidate("cand-2", "inst-1", "Lista Dos", null, null, true, Instant.now(clock), Instant.now(clock))));

    ArgumentCaptor<BallotResult> resultCaptor = ArgumentCaptor.forClass(BallotResult.class);
    when(ballotResultRepository.save(resultCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

    var response = votingService.finalizeBallot("1");

    assertThat(response.checksum()).isEqualTo("checksum-1");
    assertThat(response.finalResult()).isTrue();
    assertThat(response.results()).hasSize(2);
    assertThat(response.results().get(0).candidateId()).isEqualTo("cand-1");
    assertThat(response.results().get(0).votes()).isEqualTo(5L);
    assertThat(response.results().get(1).candidateId()).isEqualTo("cand-2");
    assertThat(response.results().get(1).votes()).isEqualTo(0L);

    BallotResult saved = resultCaptor.getValue();
    assertThat(saved.checksum()).isEqualTo("checksum-1");
    assertThat(saved.candidateVotes()).containsEntry("cand-1", 5L);
    assertThat(saved.candidateVotes()).containsEntry("cand-2", 0L);
    verify(auditService).record(eq("voting-service"), eq("BALLOT_FINALIZED"), anyMap());
  }

  @Test
  void finalizeBallotReturnsExistingSnapshotWhenAlreadyFinalized() {
    Ballot ballot = new Ballot(
        "1",
        "inst-1",
        "Elección demo",
        List.of("cand-1"),
        Instant.now(clock).minusSeconds(7200),
        Instant.now(clock).minusSeconds(60),
        false
    );
    when(ballotService.requireClosedBallot("1")).thenReturn(ballot);
    when(ballotService.emptyTally(ballot)).thenAnswer(invocation -> {
      Map<String, Long> base = new LinkedHashMap<>();
      base.put("cand-1", 0L);
      return base;
    });
    BallotResult stored = new BallotResult(
        UUID.randomUUID().toString(),
        "1",
        "inst-1",
        Map.of("cand-1", 42L),
        Instant.now(clock).minusSeconds(30),
        "checksum-final"
    );
    when(ballotResultRepository.findByBallotId("1")).thenReturn(Optional.of(stored));
    when(candidateRepository.findById("cand-1"))
        .thenReturn(Optional.of(new Candidate("cand-1", "inst-1", "Lista Uno", null, null, true, Instant.now(clock), Instant.now(clock))));

    var response = votingService.finalizeBallot("1");

    assertThat(response.checksum()).isEqualTo("checksum-final");
    assertThat(response.results().get(0).votes()).isEqualTo(42L);
    verify(ballotResultRepository, never()).save(any());
  }

  @Test
  void getFinalResultFailsWhenNotAvailable() {
    Ballot ballot = new Ballot(
        "1",
        "inst-1",
        "Elección demo",
        List.of("cand-1"),
        Instant.now(clock).minusSeconds(7200),
        Instant.now(clock).plusSeconds(3600),
        false
    );
    when(ballotService.findBallot("1")).thenReturn(Optional.of(ballot));
    when(ballotResultRepository.findByBallotId("1")).thenReturn(Optional.empty());

    assertThatThrownBy(() -> votingService.getFinalResult("1"))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Ballot result not available");
  }
}
