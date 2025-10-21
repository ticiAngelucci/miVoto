package com.mivoto.api.dto;

import java.time.Instant;

public record EligibilityResponse(String eligibilityToken, Instant expiresAt) {
}
