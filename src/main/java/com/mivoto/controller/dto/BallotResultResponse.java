package com.mivoto.controller.dto;

import java.time.Instant;
import java.util.List;

public record BallotResultResponse(
    String ballotId,
    List<TallyEntry> results,
    Instant computedAt,
    String checksum,
    boolean finalResult
) {
}
