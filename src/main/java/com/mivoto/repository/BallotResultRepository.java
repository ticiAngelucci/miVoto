package com.mivoto.repository;

import com.mivoto.model.BallotResult;
import java.util.List;
import java.util.Optional;

public interface BallotResultRepository {

  Optional<BallotResult> findByBallotId(String ballotId);

  BallotResult save(BallotResult result);

  List<BallotResult> findAll();
}
