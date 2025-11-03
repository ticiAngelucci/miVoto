package com.example.mivoto.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ballot {

    @DocumentId
    private String id;
    private String userId;
    private String electionId;
    private String status;
    private Instant votedAt;
}

