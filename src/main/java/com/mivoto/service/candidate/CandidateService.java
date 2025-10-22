package com.mivoto.service.candidate;

import com.mivoto.controller.dto.CandidateRequest;
import com.mivoto.model.Candidate;
import com.mivoto.repository.CandidateRepository;
import com.mivoto.repository.InstitutionRepository;
import com.mivoto.support.ResourceNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class CandidateService {

  private final CandidateRepository candidateRepository;
  private final InstitutionRepository institutionRepository;
  private final Clock clock;

  public CandidateService(CandidateRepository candidateRepository,
      InstitutionRepository institutionRepository,
      Clock clock) {
    this.candidateRepository = Objects.requireNonNull(candidateRepository);
    this.institutionRepository = Objects.requireNonNull(institutionRepository);
    this.clock = Objects.requireNonNull(clock);
  }

  public List<Candidate> listAll() {
    return candidateRepository.findAll();
  }

  public List<Candidate> listByInstitution(String institutionId) {
    ensureInstitutionExists(institutionId);
    return candidateRepository.findByInstitutionId(institutionId);
  }

  public Candidate get(String id) {
    return candidateRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Candidate not found: " + id));
  }

  public Candidate create(CandidateRequest request) {
    ensureInstitutionExists(request.institutionId());
    Instant now = Instant.now(clock);
    boolean active = request.active() == null || request.active();
    Candidate candidate = new Candidate(
        UUID.randomUUID().toString(),
        request.institutionId(),
        request.displayName(),
        request.listName(),
        request.biography(),
        active,
        now,
        now
    );
    return candidateRepository.save(candidate);
  }

  public Candidate update(String id, CandidateRequest request) {
    Candidate existing = get(id);
    ensureInstitutionExists(request.institutionId());
    Instant now = Instant.now(clock);
    boolean active = request.active() != null ? request.active() : existing.active();
    Candidate updated = new Candidate(
        id,
        request.institutionId(),
        request.displayName(),
        request.listName(),
        request.biography(),
        active,
        existing.createdAt(),
        now
    );
    return candidateRepository.save(updated);
  }

  public void delete(String id) {
    if (!candidateRepository.deleteById(id)) {
      throw new ResourceNotFoundException("Candidate not found: " + id);
    }
  }

  private void ensureInstitutionExists(String institutionId) {
    institutionRepository.findById(institutionId)
        .orElseThrow(() -> new ResourceNotFoundException("Institution not found: " + institutionId));
  }
}
