package com.mivoto.domain.model;

import java.time.Instant;
import java.util.List;

public record Ballot(
    String id,
    String title,
    List<String> options,
    Instant opensAt,
    Instant closesAt,
    boolean allowMultipleSelection
) {
  public boolean isOpen(Instant now) {
    return (opensAt == null || !now.isBefore(opensAt)) && (closesAt == null || now.isBefore(closesAt));
  }
}
