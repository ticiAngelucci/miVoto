package com.mivoto.controller.dto;

public record TallyEntry(
    String candidateId,
    long votes,
    String displayName,
    String listName,
    String institutionId
) {
}
