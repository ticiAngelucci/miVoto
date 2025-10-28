package com.mivoto.repository;

import com.mivoto.model.VoteRecord;
import java.util.Map;
import java.util.Optional;

public interface VoteRecordRepository {

  VoteRecord save(VoteRecord record);

  Optional<VoteRecord> findByReceipt(String receipt);

  boolean existsByBallotIdAndSubjectHash(String ballotId, String subjectHash);

  boolean existsBySubjectHash(String subjectHash);

  Map<String, Long> tallyByBallot(String ballotId);
}
