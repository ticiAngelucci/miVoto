package com.example.mivoto.model;

import com.google.cloud.firestore.annotation.DocumentId;
import lombok.Data; 
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Election {

    @DocumentId
    private String id;
    private String name;
    private String status;
    private String institutionId;
    private String voteRule;
    private String startAt;
    private String endAt;
    private List<String> candidates;
}

