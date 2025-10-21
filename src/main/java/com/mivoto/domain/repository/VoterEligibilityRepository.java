package com.mivoto.domain.repository;

import com.mivoto.domain.model.VoterEligibility;
import java.util.Optional;

public interface VoterEligibilityRepository {

  Optional<VoterEligibility> findActiveBySubjectHash(String subjectHash);

  Optional<VoterEligibility> findByTokenHash(String tokenHash);

  VoterEligibility save(VoterEligibility eligibility);

  void markConsumed(String tokenHash);
}
