package com.mivoto.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CastVoteRequest(
    @NotBlank String ballotId,
    @NotBlank String eligibilityToken,
    @NotNull CastVoteSelection selection
) {
}
