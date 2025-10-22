package com.mivoto.infrastructure.memory;

import com.mivoto.model.BallotResult;
import com.mivoto.repository.BallotResultRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("dev")
public class InMemoryBallotResultRepository implements BallotResultRepository {

  private final Map<String, BallotResult> store = new ConcurrentHashMap<>();

  @Override
  public Optional<BallotResult> findByBallotId(String ballotId) {
    return Optional.ofNullable(store.get(ballotId));
  }

  @Override
  public BallotResult save(BallotResult result) {
    store.put(result.ballotId(), result);
    return result;
  }

  @Override
  public List<BallotResult> findAll() {
    return new ArrayList<>(store.values());
  }
}
