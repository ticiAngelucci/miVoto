package com.mivoto.controller.dto;

import java.time.Instant;

public record InstitutionResponse(
    String id,
    String name,
    String description,
    boolean active,
    Instant createdAt,
    Instant updatedAt
) {
}
