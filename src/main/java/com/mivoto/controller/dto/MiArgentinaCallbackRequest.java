package com.mivoto.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record MiArgentinaCallbackRequest(
    @NotBlank String code,
    String state
) {
}
