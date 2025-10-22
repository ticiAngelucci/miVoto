package com.mivoto.model;

import java.time.Instant;

public record VoteRecord(
    String id,
    String ballotId,
    String voteHash,
    String tokenHash,
    String receipt,
    String txHash,
    Instant createdAt
) {
}
