package com.mivoto.model;

import java.time.Instant;
import java.util.List;

public record Ballot(
    String id,
    String institutionId,
    String title,
    List<String> candidateIds,
    Instant opensAt,
    Instant closesAt,
    boolean allowMultipleSelection
) {

  public Ballot {
    candidateIds = candidateIds == null ? List.of() : List.copyOf(candidateIds);
  }

  public boolean isOpen(Instant now) {
    return (opensAt == null || !now.isBefore(opensAt)) && (closesAt == null || now.isBefore(closesAt));
  }
}
