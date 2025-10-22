package com.mivoto.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record CandidateRequest(
    @NotBlank String institutionId,
    @NotBlank String displayName,
    String listName,
    String biography,
    Boolean active
) {
}
