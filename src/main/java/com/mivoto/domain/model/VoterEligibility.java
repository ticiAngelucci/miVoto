package com.mivoto.domain.model;

import java.time.Instant;

public record VoterEligibility(
    String id,
    String subjectHash,
    Instant issuedAt,
    Instant expiresAt,
    String tokenHash,
    EligibilityStatus status,
    String issuedBy
) {
}
