package com.mivoto.model;

import java.time.Instant;
import java.util.Map;
import java.util.Objects;

public record BallotResult(
    String id,
    String ballotId,
    String institutionId,
    Map<String, Long> candidateVotes,
    Instant computedAt,
    String checksum
) {

  public BallotResult {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(ballotId, "ballotId must not be null");
    Objects.requireNonNull(institutionId, "institutionId must not be null");
    Objects.requireNonNull(candidateVotes, "candidateVotes must not be null");
    Objects.requireNonNull(computedAt, "computedAt must not be null");
    Objects.requireNonNull(checksum, "checksum must not be null");
    candidateVotes = Map.copyOf(candidateVotes);
  }
}
