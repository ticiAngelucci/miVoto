package com.mivoto.infra.firebase;

import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.mivoto.domain.model.EligibilityStatus;
import com.mivoto.domain.model.VoterEligibility;
import com.mivoto.domain.repository.VoterEligibilityRepository;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class FirestoreEligibilityRepository implements VoterEligibilityRepository {

  private static final Logger log = LoggerFactory.getLogger(FirestoreEligibilityRepository.class);
  private static final String COLLECTION = "eligibilities";

  private final Firestore firestore;

  public FirestoreEligibilityRepository(Firestore firestore) {
    this.firestore = Objects.requireNonNull(firestore);
  }

  @Override
  public Optional<VoterEligibility> findActiveBySubjectHash(String subjectHash) {
    try {
      Query query = collection().whereEqualTo("subjectHash", subjectHash)
          .whereEqualTo("status", EligibilityStatus.ACTIVE.name())
          .limit(1);
      ApiFuture<QuerySnapshot> future = query.get();
      QuerySnapshot snapshot = future.get();
      if (!snapshot.isEmpty()) {
        QueryDocumentSnapshot doc = snapshot.getDocuments().get(0);
        return Optional.of(fromDocument(doc));
      }
      return Optional.empty();
    } catch (Exception e) {
      log.error("Failed to query eligibility for subjectHash", e);
      return Optional.empty();
    }
  }

  @Override
  public Optional<VoterEligibility> findByTokenHash(String tokenHash) {
    try {
      Query query = collection().whereEqualTo("tokenHash", tokenHash).limit(1);
      QuerySnapshot snapshot = query.get().get();
      if (!snapshot.isEmpty()) {
        return Optional.of(fromDocument(snapshot.getDocuments().get(0)));
      }
      return Optional.empty();
    } catch (Exception e) {
      log.error("Failed to look up eligibility by tokenHash", e);
      return Optional.empty();
    }
  }

  @Override
  public VoterEligibility save(VoterEligibility eligibility) {
    String id = eligibility.id() != null ? eligibility.id() : UUID.randomUUID().toString();
    DocumentReference doc = collection().document(id);
    doc.set(toDocument(eligibility)).isDone();
    return new VoterEligibility(
        id,
        eligibility.subjectHash(),
        eligibility.issuedAt(),
        eligibility.expiresAt(),
        eligibility.tokenHash(),
        eligibility.status(),
        eligibility.issuedBy()
    );
  }

  @Override
  public void markConsumed(String tokenHash) {
    findByTokenHash(tokenHash).ifPresent(eligibility -> {
      DocumentReference doc = collection().document(eligibility.id());
      doc.update("status", EligibilityStatus.CONSUMED.name());
    });
  }

  private CollectionReference collection() {
    return firestore.collection(COLLECTION);
  }

  private Map<String, Object> toDocument(VoterEligibility eligibility) {
    return Map.of(
        "subjectHash", eligibility.subjectHash(),
        "issuedAt", Timestamp.ofTimeSecondsAndNanos(eligibility.issuedAt().getEpochSecond(), eligibility.issuedAt().getNano()),
        "expiresAt", Timestamp.ofTimeSecondsAndNanos(eligibility.expiresAt().getEpochSecond(), eligibility.expiresAt().getNano()),
        "tokenHash", eligibility.tokenHash(),
        "status", eligibility.status().name(),
        "issuedBy", eligibility.issuedBy()
    );
  }

  private VoterEligibility fromDocument(DocumentSnapshot document) {
    Instant issuedAt = document.getTimestamp("issuedAt").toInstant();
    Instant expiresAt = document.getTimestamp("expiresAt").toInstant();
    String subjectHash = document.getString("subjectHash");
    String tokenHash = document.getString("tokenHash");
    String issuedBy = document.getString("issuedBy");
    EligibilityStatus status = EligibilityStatus.valueOf(document.getString("status"));
    return new VoterEligibility(document.getId(), subjectHash, issuedAt, expiresAt, tokenHash, status, issuedBy);
  }
}
