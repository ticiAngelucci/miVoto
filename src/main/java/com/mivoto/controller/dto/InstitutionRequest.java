package com.mivoto.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record InstitutionRequest(
    @NotBlank String name,
    String description,
    Boolean active
) {
}
