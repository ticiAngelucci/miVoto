package com.mivoto.model;

import java.time.Instant;

public record VoterEligibility(
    String id,
    String subjectHash,
    Instant issuedAt,
    Instant expiresAt,
    String tokenHash,
    String walletAddress,
    EligibilityStatus status,
    String issuedBy
) {
}
