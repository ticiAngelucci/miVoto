package com.mivoto.infrastructure.firebase;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.mivoto.model.Candidate;
import com.mivoto.repository.CandidateRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!dev")
public class FirestoreCandidateRepository implements CandidateRepository {

  private static final Logger log = LoggerFactory.getLogger(FirestoreCandidateRepository.class);
  private static final String COLLECTION = "candidatos";

  private final Firestore firestore;

  public FirestoreCandidateRepository(Firestore firestore) {
    this.firestore = Objects.requireNonNull(firestore);
  }

  @Override
  public List<Candidate> findAll() {
    try {
      List<QueryDocumentSnapshot> docs = collection().orderBy("displayName").get().get().getDocuments();
      List<Candidate> result = new ArrayList<>();
      for (DocumentSnapshot doc : docs) {
        result.add(fromDocument(doc));
      }
      return result;
    } catch (Exception e) {
      log.error("Failed to list candidates", e);
      return List.of();
    }
  }

  @Override
  public List<Candidate> findByInstitutionId(String institutionId) {
    try {
      Query query = collection().whereEqualTo("institutionId", institutionId).orderBy("displayName");
      QuerySnapshot snapshot = query.get().get();
      List<Candidate> result = new ArrayList<>();
      for (DocumentSnapshot doc : snapshot.getDocuments()) {
        result.add(fromDocument(doc));
      }
      return result;
    } catch (Exception e) {
      log.error("Failed to list candidates for institution {}", institutionId, e);
      return List.of();
    }
  }

  @Override
  public Optional<Candidate> findById(String id) {
    try {
      DocumentSnapshot doc = collection().document(id).get().get();
      if (!doc.exists()) {
        return Optional.empty();
      }
      return Optional.of(fromDocument(doc));
    } catch (Exception e) {
      log.error("Failed to fetch candidate {}", id, e);
      return Optional.empty();
    }
  }

  @Override
  public Candidate save(Candidate candidate) {
    String id = candidate.id() != null ? candidate.id() : UUID.randomUUID().toString();
    Candidate normalized = new Candidate(
        id,
        candidate.institutionId(),
        candidate.displayName(),
        candidate.listName(),
        candidate.biography(),
        candidate.active(),
        candidate.createdAt(),
        candidate.updatedAt()
    );
    DocumentReference doc = collection().document(normalized.id());
    try {
      doc.set(toDocument(normalized)).get();
      return normalized;
    } catch (Exception e) {
      throw new IllegalStateException("Failed to persist candidate", e);
    }
  }

  @Override
  public boolean deleteById(String id) {
    DocumentReference doc = collection().document(id);
    try {
      doc.delete().get();
      return true;
    } catch (Exception e) {
      log.error("Failed to delete candidate {}", id, e);
      return false;
    }
  }

  private CollectionReference collection() {
    return firestore.collection(COLLECTION);
  }

  private Map<String, Object> toDocument(Candidate candidate) {
    return Map.of(
        "institutionId", candidate.institutionId(),
        "displayName", candidate.displayName(),
        "listName", candidate.listName(),
        "biography", candidate.biography(),
        "active", candidate.active(),
        "createdAt", toTimestamp(candidate.createdAt()),
        "updatedAt", toTimestamp(candidate.updatedAt())
    );
  }

  private Candidate fromDocument(DocumentSnapshot doc) {
    Instant createdAt = doc.contains("createdAt") ? doc.getTimestamp("createdAt").toDate().toInstant() : Instant.EPOCH;
    Instant updatedAt = doc.contains("updatedAt") ? doc.getTimestamp("updatedAt").toDate().toInstant() : createdAt;
    return new Candidate(
        doc.getId(),
        doc.getString("institutionId"),
        doc.getString("displayName"),
        doc.getString("listName"),
        doc.getString("biography"),
        Boolean.TRUE.equals(doc.getBoolean("active")),
        createdAt,
        updatedAt
    );
  }

  private Timestamp toTimestamp(Instant instant) {
    return Timestamp.ofTimeSecondsAndNanos(instant.getEpochSecond(), instant.getNano());
  }
}
