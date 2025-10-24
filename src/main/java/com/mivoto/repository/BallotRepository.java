package com.mivoto.repository;

import com.mivoto.model.Ballot;
import java.util.List;
import java.util.Optional;

public interface BallotRepository {

  Optional<Ballot> findById(String id);

  List<Ballot> findAll();

  Ballot save(Ballot ballot);
}
