package com.mivoto.api.dto;

import jakarta.validation.constraints.NotBlank;

public record MiArgentinaCallbackRequest(
    @NotBlank String code,
    @NotBlank String state
) {
}
