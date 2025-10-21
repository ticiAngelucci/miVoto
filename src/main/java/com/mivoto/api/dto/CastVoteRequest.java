package com.mivoto.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Map;

public record CastVoteRequest(
    @NotBlank String ballotId,
    @NotBlank String eligibilityToken,
    @NotNull Map<String, Object> votePayload
) {
}
