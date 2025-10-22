package com.mivoto.model;

import java.time.Instant;
import java.util.Objects;

public record Institution(
    String id,
    String name,
    String description,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {

  public Institution {
    Objects.requireNonNull(id, "id must not be null");
    Objects.requireNonNull(name, "name must not be null");
    Objects.requireNonNull(createdAt, "createdAt must not be null");
    Objects.requireNonNull(updatedAt, "updatedAt must not be null");
  }

  public Institution withUpdatedFields(String name, String description, boolean active, Instant updatedAt) {
    return new Institution(id, name, description, active, createdAt, Objects.requireNonNull(updatedAt));
  }
}
