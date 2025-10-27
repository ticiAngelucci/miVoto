package com.mivoto.infrastructure.memory;

import com.mivoto.model.EligibilityStatus;
import com.mivoto.model.VoterEligibility;
import com.mivoto.repository.VoterEligibilityRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("dev")
public class InMemoryVoterEligibilityRepository implements VoterEligibilityRepository {

  private final Map<String, VoterEligibility> store = new ConcurrentHashMap<>();

  @Override
  public Optional<VoterEligibility> findActiveBySubjectHash(String subjectHash) {
    return store.values().stream()
        .filter(value -> value.subjectHash().equals(subjectHash))
        .filter(value -> value.status() == EligibilityStatus.ACTIVE)
        .filter(value -> value.expiresAt() == null || value.expiresAt().isAfter(Instant.now()))
        .findFirst();
  }

  @Override
  public Optional<VoterEligibility> findByTokenHash(String tokenHash) {
    return store.values().stream()
        .filter(value -> value.tokenHash().equals(tokenHash))
        .findFirst();
  }

  @Override
  public VoterEligibility save(VoterEligibility eligibility) {
    String id = eligibility.id() != null ? eligibility.id() : UUID.randomUUID().toString();
    VoterEligibility normalized = new VoterEligibility(
        id,
        eligibility.subjectHash(),
        eligibility.issuedAt(),
        eligibility.expiresAt(),
        eligibility.tokenHash(),
        eligibility.walletAddress(),
        eligibility.status(),
        eligibility.issuedBy()
    );
    store.put(id, normalized);
    return normalized;
  }

  @Override
  public void markConsumed(String tokenHash) {
    findByTokenHash(tokenHash).ifPresent(record -> {
      VoterEligibility updated = new VoterEligibility(
          record.id(),
          record.subjectHash(),
          record.issuedAt(),
          record.expiresAt(),
          record.tokenHash(),
          record.walletAddress(),
          EligibilityStatus.CONSUMED,
          record.issuedBy()
      );
      store.put(updated.id(), updated);
    });
  }
}
