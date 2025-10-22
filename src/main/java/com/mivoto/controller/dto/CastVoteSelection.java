package com.mivoto.controller.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record CastVoteSelection(
    @NotBlank String institutionId,
    @NotEmpty List<@NotBlank String> candidateIds
) {
}
