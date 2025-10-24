package com.mivoto.service.admin;

import com.mivoto.config.SeedProperties;
import com.mivoto.model.Ballot;
import com.mivoto.model.Candidate;
import com.mivoto.model.Institution;
import com.mivoto.repository.BallotRepository;
import com.mivoto.repository.CandidateRepository;
import com.mivoto.repository.InstitutionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class SeedService {

  private final InstitutionRepository institutionRepository;
  private final CandidateRepository candidateRepository;
  private final BallotRepository ballotRepository;
  private final SeedProperties seedProperties;
  private final Clock clock;

  public SeedService(InstitutionRepository institutionRepository,
      CandidateRepository candidateRepository,
      BallotRepository ballotRepository,
      SeedProperties seedProperties,
      Clock clock) {
    this.institutionRepository = Objects.requireNonNull(institutionRepository);
    this.candidateRepository = Objects.requireNonNull(candidateRepository);
    this.ballotRepository = Objects.requireNonNull(ballotRepository);
    this.seedProperties = Objects.requireNonNull(seedProperties);
    this.clock = Objects.requireNonNull(clock);
  }

  public boolean isEnabled() {
    return seedProperties.enabled();
  }

  public SeedSummary seedDefaultData() {
    Instant now = Instant.now(clock);

    boolean createdInstitution = ensureInstitution(new Institution(
        "inst-1",
        "Institución Demo",
        "Entidad ficticia para pruebas",
        true,
        now,
        now
    ));

    boolean createdCandidate1 = ensureCandidate(new Candidate(
        "cand-1",
        "inst-1",
        "Lista Azul",
        "Lista Azul",
        "Propuesta azul",
        true,
        now,
        now
    ));
    boolean createdCandidate2 = ensureCandidate(new Candidate(
        "cand-2",
        "inst-1",
        "Lista Verde",
        "Lista Verde",
        "Propuesta verde",
        true,
        now,
        now
    ));

    boolean createdBallot = ensureBallot(new Ballot(
        "1",
        "inst-1",
        "Consulta popular de prueba",
        List.of("cand-1", "cand-2"),
        now.minus(1, ChronoUnit.HOURS),
        now.plus(1, ChronoUnit.DAYS),
        false
    ));

    return new SeedSummary(
        createdInstitution ? 1 : 0,
        (createdCandidate1 ? 1 : 0) + (createdCandidate2 ? 1 : 0),
        createdBallot ? 1 : 0
    );
  }

  public Ballot closeBallotNow(String ballotId) {
    Ballot ballot = ballotRepository.findById(ballotId)
        .orElseThrow(() -> new IllegalArgumentException("Boleta no encontrada: " + ballotId));
    Instant now = Instant.now(clock);
    Ballot closed = new Ballot(
        ballot.id(),
        ballot.institutionId(),
        ballot.title(),
        ballot.candidateIds(),
        ballot.opensAt(),
        now.minus(1, ChronoUnit.SECONDS),
        ballot.allowMultipleSelection()
    );
    ballotRepository.save(closed);
    return closed;
  }

  private boolean ensureInstitution(Institution institution) {
    boolean created = institutionRepository.findById(institution.id()).isEmpty();
    institutionRepository.save(institution);
    return created;
  }

  private boolean ensureCandidate(Candidate candidate) {
    boolean created = candidateRepository.findById(candidate.id()).isEmpty();
    candidateRepository.save(candidate);
    return created;
  }

  private boolean ensureBallot(Ballot ballot) {
    boolean created = ballotRepository.findById(ballot.id()).isEmpty();
    ballotRepository.save(ballot);
    return created;
  }

  public record SeedSummary(int institutionsCreated, int candidatesCreated, int ballotsCreated) {
    public boolean changed() {
      return institutionsCreated > 0 || candidatesCreated > 0 || ballotsCreated > 0;
    }
  }
}
