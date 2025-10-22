package com.mivoto.infrastructure.memory;

import com.mivoto.model.VoteRecord;
import com.mivoto.repository.VoteRecordRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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
        record.voteHash(),
        record.tokenHash(),
        record.receipt(),
        record.txHash(),
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
  public Map<String, Long> tallyByBallot(String ballotId) {
    return store.values().stream()
        .filter(record -> record.ballotId().equals(ballotId))
        .collect(Collectors.groupingBy(VoteRecord::voteHash, Collectors.counting()));
  }
}
