package com.mivoto.domain.repository;

import com.mivoto.domain.model.VoteRecord;
import java.util.Map;
import java.util.Optional;

public interface VoteRecordRepository {

  VoteRecord save(VoteRecord record);

  Optional<VoteRecord> findByReceipt(String receipt);

  Map<String, Long> tallyByBallot(String ballotId);
}
