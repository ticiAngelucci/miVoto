package com.mivoto.repository;

import com.mivoto.model.VoterEligibility;
import java.util.Optional;

public interface VoterEligibilityRepository {

  Optional<VoterEligibility> findActiveBySubjectHash(String subjectHash);

  Optional<VoterEligibility> findByTokenHash(String tokenHash);

  VoterEligibility save(VoterEligibility eligibility);

  void markConsumed(String tokenHash);
}
