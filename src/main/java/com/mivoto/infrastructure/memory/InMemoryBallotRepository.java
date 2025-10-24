package com.mivoto.infrastructure.memory;

import com.mivoto.model.Ballot;
import com.mivoto.repository.BallotRepository;
import java.time.Instant;
import java.util.ArrayList;
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
        "inst-1",
        "Consulta popular de prueba",
        List.of("cand-1", "cand-2"),
        Instant.now().minusSeconds(3600),
        Instant.now().plusSeconds(86400),
        false
    ));
  }

  @Override
  public Optional<Ballot> findById(String id) {
    return Optional.ofNullable(ballots.get(id));
  }

  @Override
  public List<Ballot> findAll() {
    return new ArrayList<>(ballots.values());
  }

  @Override
  public Ballot save(Ballot ballot) {
    ballots.put(ballot.id(), ballot);
    return ballot;
  }
}
