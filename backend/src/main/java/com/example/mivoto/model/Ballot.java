package com.example.mivoto.model;

import com.google.cloud.firestore.annotation.DocumentId;
import java.time.Instant;

public record Ballot(
    @DocumentId String id,
    String userId,
    String electionId,
    String status,
    Instant votedAt // Para saber cuándo votó
) {}