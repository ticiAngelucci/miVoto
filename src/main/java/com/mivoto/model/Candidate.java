package com.mivoto.model;

import java.time.Instant;
import java.util.Objects;

public record Candidate(
    String id,
    String institutionId,
    String displayName,
    String listName,
    String biography,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {

  public Candidate {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(institutionId, "institutionId must not be null");
    Objects.requireNonNull(displayName, "displayName must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
    Objects.requireNonNull(updatedAt, "updatedAt must not be null");
  }

  public Candidate withUpdatedFields(
      String displayName,
      String listName,
      String biography,
      boolean active,
      Instant updatedAt
  ) {
    return new Candidate(
        id,
        institutionId,
        displayName,
        listName,
        biography,
        active,
        createdAt,
        Objects.requireNonNull(updatedAt)
    );
  }
}
