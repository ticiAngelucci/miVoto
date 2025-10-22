package com.mivoto.infrastructure.firebase;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.mivoto.model.Institution;
import com.mivoto.repository.InstitutionRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!dev")
public class FirestoreInstitutionRepository implements InstitutionRepository {

  private static final Logger log = LoggerFactory.getLogger(FirestoreInstitutionRepository.class);
  private static final String COLLECTION = "instituciones";

  private final Firestore firestore;

  public FirestoreInstitutionRepository(Firestore firestore) {
    this.firestore = Objects.requireNonNull(firestore);
  }

  @Override
  public List<Institution> findAll() {
    try {
      List<QueryDocumentSnapshot> docs = collection().orderBy("name").get().get().getDocuments();
      List<Institution> result = new ArrayList<>();
      for (DocumentSnapshot doc : docs) {
        result.add(fromDocument(doc));
      }
      return result;
    } catch (Exception e) {
      log.error("Failed to list institutions", e);
      return List.of();
    }
  }

  @Override
  public Optional<Institution> findById(String id) {
    try {
      DocumentSnapshot doc = collection().document(id).get().get();
      if (!doc.exists()) {
        return Optional.empty();
      }
      return Optional.of(fromDocument(doc));
    } catch (Exception e) {
      log.error("Failed to fetch institution {}", id, e);
      return Optional.empty();
    }
  }

  @Override
  public Institution save(Institution institution) {
    DocumentReference doc = collection().document(institution.id());
    try {
      doc.set(toDocument(institution)).get();
      return institution;
    } catch (Exception e) {
      throw new IllegalStateException("Failed to persist institution", e);
    }
  }

  @Override
  public boolean deleteById(String id) {
    DocumentReference doc = collection().document(id);
    try {
      doc.delete().get();
      return true;
    } catch (Exception e) {
      log.error("Failed to delete institution {}", id, e);
      return false;
    }
  }

  private CollectionReference collection() {
    return firestore.collection(COLLECTION);
  }

  private Map<String, Object> toDocument(Institution institution) {
    return Map.of(
        "name", institution.name(),
        "description", institution.description(),
        "active", institution.active(),
        "createdAt", toTimestamp(institution.createdAt()),
        "updatedAt", toTimestamp(institution.updatedAt())
    );
  }

  private Institution fromDocument(DocumentSnapshot doc) {
    Instant createdAt = doc.contains("createdAt") ? doc.getTimestamp("createdAt").toDate().toInstant() : Instant.EPOCH;
    Instant updatedAt = doc.contains("updatedAt") ? doc.getTimestamp("updatedAt").toDate().toInstant() : createdAt;
    return new Institution(
        doc.getId(),
        doc.getString("name"),
        doc.getString("description"),
        Boolean.TRUE.equals(doc.getBoolean("active")),
        createdAt,
        updatedAt
    );
  }

  private Timestamp toTimestamp(Instant instant) {
    return Timestamp.ofTimeSecondsAndNanos(instant.getEpochSecond(), instant.getNano());
  }
}
