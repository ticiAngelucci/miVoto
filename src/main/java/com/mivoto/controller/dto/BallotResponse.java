package com.mivoto.controller.dto;

import java.time.Instant;
import java.util.List;

public record BallotResponse(
    String id,
    String institutionId,
    String title,
    List<String> candidateIds,
    Instant opensAt,
    Instant closesAt,
    boolean allowMultipleSelection,
    boolean open
) {
}
