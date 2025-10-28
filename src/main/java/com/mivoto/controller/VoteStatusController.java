package com.mivoto.controller;

import com.mivoto.repository.VoteRecordRepository;
import com.mivoto.support.HashingService;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/internal/vote-status")
public class VoteStatusController {

  private final VoteRecordRepository voteRecordRepository;
  private final HashingService hashingService;

  public VoteStatusController(VoteRecordRepository voteRecordRepository,
      HashingService hashingService) {
    this.voteRecordRepository = Objects.requireNonNull(voteRecordRepository);
    this.hashingService = Objects.requireNonNull(hashingService);
  }

  @GetMapping
  public List<VoteStatusResponse> status(@RequestParam(value = "subjects") List<String> subjects) {
    if (subjects == null || subjects.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "subjects parameter required");
    }
    List<String> normalized = subjects.stream()
        .flatMap(value -> Arrays.stream(value.split(",")))
        .map(String::trim)
        .filter(s -> !s.isEmpty())
        .distinct()
        .toList();
    if (normalized.isEmpty()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "subjects parameter required");
    }
    return normalized.stream()
        .map(subject -> new VoteStatusResponse(subject, hasAnyVote(subject)))
        .collect(Collectors.toList());
  }

  private boolean hasAnyVote(String subject) {
    String subjectHash = hashingService.hashSubject(subject);
    return voteRecordRepository.existsBySubjectHash(subjectHash);
  }

  public record VoteStatusResponse(String subject, boolean hasVoted) {
  }
}
