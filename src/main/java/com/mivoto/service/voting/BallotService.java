package com.mivoto.service.voting;

import com.mivoto.model.Ballot;
import com.mivoto.repository.BallotRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
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

  public Ballot requireClosedBallot(String ballotId) {
    Ballot ballot = ballotRepository.findById(ballotId)
        .orElseThrow(() -> new IllegalArgumentException("Ballot not found"));
    Instant now = Instant.now(clock);
    if (ballot.isOpen(now)) {
      throw new IllegalStateException("Ballot is still open");
    }
    return ballot;
  }

  public Optional<Ballot> findBallot(String ballotId) {
    return ballotRepository.findById(ballotId);
  }

  public List<Ballot> listBallots() {
    return ballotRepository.findAll();
  }

  public Map<String, Long> emptyTally(Ballot ballot) {
    Map<String, Long> counts = new LinkedHashMap<>();
    for (String candidateId : ballot.candidateIds()) {
      counts.put(candidateId, 0L);
    }
    return counts;
  }
}
