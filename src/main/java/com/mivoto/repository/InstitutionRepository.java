package com.mivoto.repository;

import com.mivoto.model.Institution;
import java.util.List;
import java.util.Optional;

public interface InstitutionRepository {

  List<Institution> findAll();

  Optional<Institution> findById(String id);

  Institution save(Institution institution);

  boolean deleteById(String id);
}
