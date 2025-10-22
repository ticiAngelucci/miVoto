package com.mivoto.model;

import java.time.Instant;

public record VoteReceipt(
    String receipt,
    String ballotId,
    String txHash,
    Instant timestamp
) {
}
