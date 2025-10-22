package com.mivoto.controller.dto;

import java.time.Instant;

public record EligibilityResponse(String eligibilityToken, Instant expiresAt) {
}
