package com.mivoto.service.voting;

import com.mivoto.domain.model.Ballot;
import com.mivoto.domain.repository.BallotRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class BallotService {

  private final BallotRepository ballotRepository;
  private final Clock clock;

  public BallotService(BallotRepository ballotRepository, Clock clock) {
    this.ballotRepository = Objects.requireNonNull(ballotRepository);
    this.clock = Objects.requireNonNull(clock);
  }

  public Ballot requireOpenBallot(String ballotId) {
    Ballot ballot = ballotRepository.findById(ballotId)
        .orElseThrow(() -> new IllegalArgumentException("Ballot not found"));
    Instant now = Instant.now(clock);
    if (!ballot.isOpen(now)) {
      throw new IllegalStateException("Ballot is not accepting votes");
    }
    return ballot;
  }

  public Optional<Ballot> findBallot(String ballotId) {
    return ballotRepository.findById(ballotId);
  }

  public Map<String, Long> emptyTally() {
    // TODO: implementar cálculo real de tally a partir de resultados.
    return Map.of();
  }
}
