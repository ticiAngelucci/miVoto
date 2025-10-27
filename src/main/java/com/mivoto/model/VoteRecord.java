package com.mivoto.model;

import java.time.Instant;
import java.util.List;

public record VoteRecord(
    String id,
    String ballotId,
    String institutionId,
    List<String> candidateIds,
    String voteHash,
    String tokenHash,
    String subjectHash,
    String receipt,
    String txHash,
    String sbtTokenId,
    Instant createdAt
) {

  public VoteRecord {
    candidateIds = candidateIds == null ? List.of() : List.copyOf(candidateIds);
  }
}
