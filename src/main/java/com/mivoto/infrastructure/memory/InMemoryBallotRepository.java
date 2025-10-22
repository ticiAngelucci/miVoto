package com.mivoto.infrastructure.memory;

import com.mivoto.model.Ballot;
import com.mivoto.repository.BallotRepository;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("dev")
public class InMemoryBallotRepository implements BallotRepository {

  private final Map<String, Ballot> ballots = new ConcurrentHashMap<>();

  public InMemoryBallotRepository() {
    ballots.put("1", new Ballot(
        "1",
        "Consulta popular de prueba",
        List.of("SI", "NO"),
        Instant.now().minusSeconds(3600),
        Instant.now().plusSeconds(86400),
        false
    ));
  }

  @Override
  public Optional<Ballot> findById(String id) {
    return Optional.ofNullable(ballots.get(id));
  }
}
