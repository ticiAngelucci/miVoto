package com.mivoto.infrastructure.memory;

import com.mivoto.model.Candidate;
import com.mivoto.repository.CandidateRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("dev")
public class InMemoryCandidateRepository implements CandidateRepository {

  private final Map<String, Candidate> store = new ConcurrentHashMap<>();
  private final Clock clock;

  public InMemoryCandidateRepository(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    Instant now = Instant.now(clock);
    store.put("cand-1", new Candidate("cand-1", "inst-1", "Lista Azul", "Lista Azul", "Candidatura de demostración", true, now, now));
    store.put("cand-2", new Candidate("cand-2", "inst-1", "Lista Verde", "Lista Verde", "Propuesta verde ficticia", true, now, now));
  }

  @Override
  public List<Candidate> findAll() {
    return store.values().stream()
        .sorted(Comparator.comparing(Candidate::displayName))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Override
  public List<Candidate> findByInstitutionId(String institutionId) {
    return store.values().stream()
        .filter(candidate -> candidate.institutionId().equals(institutionId))
        .sorted(Comparator.comparing(Candidate::displayName))
        .collect(Collectors.toCollection(ArrayList::new));
  }

  @Override
  public Optional<Candidate> findById(String id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public Candidate save(Candidate candidate) {
    store.put(candidate.id(), candidate);
    return candidate;
  }

  @Override
  public boolean deleteById(String id) {
    return store.remove(id) != null;
  }
}
