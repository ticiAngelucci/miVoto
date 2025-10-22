package com.mivoto.controller.dto;

import java.time.Instant;

public record CandidateResponse(
    String id,
    String institutionId,
    String displayName,
    String listName,
    String biography,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
}
