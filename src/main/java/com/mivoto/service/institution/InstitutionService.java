package com.mivoto.service.institution;

import com.mivoto.controller.dto.InstitutionRequest;
import com.mivoto.model.Institution;
import com.mivoto.repository.InstitutionRepository;
import com.mivoto.support.ResourceNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class InstitutionService {

  private final InstitutionRepository institutionRepository;
  private final Clock clock;

  public InstitutionService(InstitutionRepository institutionRepository, Clock clock) {
    this.institutionRepository = Objects.requireNonNull(institutionRepository);
    this.clock = Objects.requireNonNull(clock);
  }

  public List<Institution> listAll() {
    return institutionRepository.findAll();
  }

  public Institution get(String id) {
    return institutionRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Institution not found: " + id));
  }

  public Institution create(InstitutionRequest request) {
    Instant now = Instant.now(clock);
    String id = UUID.randomUUID().toString();
    boolean active = request.active() == null || request.active();
    Institution institution = new Institution(
        id,
        request.name(),
        request.description(),
        active,
        now,
        now
    );
    return institutionRepository.save(institution);
  }

  public Institution update(String id, InstitutionRequest request) {
    Institution existing = get(id);
    Instant now = Instant.now(clock);
    boolean active = request.active() != null ? request.active() : existing.active();
    Institution updated = new Institution(
        id,
        request.name(),
        request.description(),
        active,
        existing.createdAt(),
        now
    );
    return institutionRepository.save(updated);
  }

  public void delete(String id) {
    if (!institutionRepository.deleteById(id)) {
      throw new ResourceNotFoundException("Institution not found: " + id);
    }
  }
}
