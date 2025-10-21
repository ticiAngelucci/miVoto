package com.mivoto.api.dto;

import jakarta.validation.constraints.NotBlank;

public record EligibilityRequest(@NotBlank String idToken) {
}
