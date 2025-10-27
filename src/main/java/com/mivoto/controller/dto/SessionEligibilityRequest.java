package com.mivoto.controller.dto;

import jakarta.validation.constraints.NotBlank;

public record SessionEligibilityRequest(@NotBlank String walletAddress) {
}
