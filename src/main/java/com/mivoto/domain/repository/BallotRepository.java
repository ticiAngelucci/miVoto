package com.mivoto.domain.repository;

import com.mivoto.domain.model.Ballot;
import java.util.Optional;

public interface BallotRepository {

  Optional<Ballot> findById(String id);
}
