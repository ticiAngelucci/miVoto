package com.mivoto.controller;

import com.mivoto.controller.dto.CandidateRequest;
import com.mivoto.controller.dto.CandidateResponse;
import com.mivoto.model.Candidate;
import com.mivoto.service.candidate.CandidateService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/candidates")
public class CandidateController {

  private final CandidateService candidateService;

  public CandidateController(CandidateService candidateService) {
    this.candidateService = candidateService;
  }

  @GetMapping
  public ResponseEntity<List<CandidateResponse>> list(@RequestParam(name = "institutionId", required = false) String institutionId) {
    List<Candidate> candidates = institutionId != null
        ? candidateService.listByInstitution(institutionId)
        : candidateService.listAll();
    List<CandidateResponse> responses = candidates.stream()
        .map(this::toResponse)
        .collect(Collectors.toList());
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CandidateResponse> get(@PathVariable String id) {
    return ResponseEntity.ok(toResponse(candidateService.get(id)));
  }

  @PostMapping
  public ResponseEntity<CandidateResponse> create(@RequestBody @Valid CandidateRequest request) {
    Candidate created = candidateService.create(request);
    return ResponseEntity
        .created(URI.create("/candidates/" + created.id()))
        .body(toResponse(created));
  }

  @PutMapping("/{id}")
  public ResponseEntity<CandidateResponse> update(
      @PathVariable String id,
      @RequestBody @Valid CandidateRequest request) {
    Candidate updated = candidateService.update(id, request);
    return ResponseEntity.ok(toResponse(updated));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    candidateService.delete(id);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
  }

  private CandidateResponse toResponse(Candidate candidate) {
    return new CandidateResponse(
        candidate.id(),
        candidate.institutionId(),
        candidate.displayName(),
        candidate.listName(),
        candidate.biography(),
        candidate.active(),
        candidate.createdAt(),
        candidate.updatedAt()
    );
  }
}
