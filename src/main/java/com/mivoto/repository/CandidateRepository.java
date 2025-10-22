package com.mivoto.repository;

import com.mivoto.model.Candidate;
import java.util.List;
import java.util.Optional;

public interface CandidateRepository {

  List<Candidate> findAll();

  List<Candidate> findByInstitutionId(String institutionId);

  Optional<Candidate> findById(String id);

  Candidate save(Candidate candidate);

  boolean deleteById(String id);
}
