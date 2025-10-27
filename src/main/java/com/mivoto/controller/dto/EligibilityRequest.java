package com.mivoto.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record EligibilityRequest(
    @NotBlank String idToken,
    @NotBlank String walletAddress
) {
}
