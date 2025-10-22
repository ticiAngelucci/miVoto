package com.mivoto.infrastructure.memory;

import com.mivoto.model.Institution;
import com.mivoto.repository.InstitutionRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("dev")
public class InMemoryInstitutionRepository implements InstitutionRepository {

  private final Map<String, Institution> store = new ConcurrentHashMap<>();
  private final Clock clock;

  public InMemoryInstitutionRepository(Clock clock) {
    this.clock = Objects.requireNonNull(clock);
    Instant now = Instant.now(clock);
    store.put("inst-1", new Institution("inst-1", "Institución Demo", "Entidad ficticia para pruebas", true, now, now));
    store.put("inst-2", new Institution("inst-2", "Institución Observadora", "Organismo observador invitado", true, now, now));
  }

  @Override
  public List<Institution> findAll() {
    List<Institution> institutions = new ArrayList<>(store.values());
    institutions.sort(Comparator.comparing(Institution::name));
    return institutions;
  }

  @Override
  public Optional<Institution> findById(String id) {
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public Institution save(Institution institution) {
    store.put(institution.id(), institution);
    return institution;
  }

  @Override
  public boolean deleteById(String id) {
    return store.remove(id) != null;
  }
}
