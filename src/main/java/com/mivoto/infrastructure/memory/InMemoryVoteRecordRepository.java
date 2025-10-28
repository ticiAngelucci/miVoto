package com.mivoto.infrastructure.memory;

import com.mivoto.model.VoteRecord;
import com.mivoto.repository.VoteRecordRepository;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("dev")
public class InMemoryVoteRecordRepository implements VoteRecordRepository {

  private final Map<String, VoteRecord> store = new ConcurrentHashMap<>();

  @Override
  public VoteRecord save(VoteRecord record) {
    String id = record.id() != null ? record.id() : UUID.randomUUID().toString();
    VoteRecord normalized = new VoteRecord(
        id,
        record.ballotId(),
        record.institutionId(),
        record.candidateIds() != null ? List.copyOf(record.candidateIds()) : List.of(),
        record.voteHash(),
        record.tokenHash(),
        record.subjectHash(),
        record.receipt(),
        record.txHash(),
        record.sbtTokenId(),
        record.createdAt() != null ? record.createdAt() : Instant.now()
    );
    store.put(id, normalized);
    return normalized;
  }

  @Override
  public Optional<VoteRecord> findByReceipt(String receipt) {
    return store.values().stream()
        .filter(record -> record.receipt().equals(receipt))
        .findFirst();
  }

  @Override
  public boolean existsByBallotIdAndSubjectHash(String ballotId, String subjectHash) {
    return store.values().stream()
        .anyMatch(record -> record.ballotId().equals(ballotId)
            && record.subjectHash() != null
            && record.subjectHash().equals(subjectHash));
  }

  @Override
  public Map<String, Long> tallyByBallot(String ballotId) {
    Map<String, Long> counts = new HashMap<>();
    store.values().stream()
        .filter(record -> record.ballotId().equals(ballotId))
        .forEach(record -> record.candidateIds().forEach(candidateId ->
            counts.merge(candidateId, 1L, Long::sum)));
    return counts;
  }

  @Override
  public boolean existsBySubjectHash(String subjectHash) {
    return store.values().stream()
        .anyMatch(record -> record.subjectHash() != null && record.subjectHash().equals(subjectHash));
  }
}
