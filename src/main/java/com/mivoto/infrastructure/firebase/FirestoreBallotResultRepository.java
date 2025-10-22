package com.mivoto.infrastructure.firebase;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.mivoto.model.BallotResult;
import com.mivoto.repository.BallotResultRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
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
public class FirestoreBallotResultRepository implements BallotResultRepository {

  private static final Logger log = LoggerFactory.getLogger(FirestoreBallotResultRepository.class);
  private static final String COLLECTION = "resultadosBoleta";

  private final Firestore firestore;

  public FirestoreBallotResultRepository(Firestore firestore) {
    this.firestore = Objects.requireNonNull(firestore);
  }

  @Override
  public Optional<BallotResult> findByBallotId(String ballotId) {
    try {
      DocumentSnapshot doc = collection().document(ballotId).get().get();
      if (!doc.exists()) {
        return Optional.empty();
      }
      return Optional.of(fromDocument(doc));
    } catch (Exception e) {
      log.error("Failed to fetch ballot result {}", ballotId, e);
      return Optional.empty();
    }
  }

  @Override
  public BallotResult save(BallotResult result) {
    String documentId = result.ballotId();
    DocumentReference doc = collection().document(documentId);
    try {
      doc.set(toDocument(result)).get();
      return result;
    } catch (Exception e) {
      throw new IllegalStateException("Failed to persist ballot result", e);
    }
  }

  @Override
  public List<BallotResult> findAll() {
    try {
      QuerySnapshot snapshot = collection().get().get();
      List<BallotResult> results = new ArrayList<>();
      for (QueryDocumentSnapshot doc : snapshot.getDocuments()) {
        results.add(fromDocument(doc));
      }
      return results;
    } catch (Exception e) {
      log.error("Failed to list ballot results", e);
      return List.of();
    }
  }

  private CollectionReference collection() {
    return firestore.collection(COLLECTION);
  }

  private Map<String, Object> toDocument(BallotResult result) {
    Map<String, Object> data = new HashMap<>();
    data.put("id", result.id());
    data.put("ballotId", result.ballotId());
    data.put("institutionId", result.institutionId());
    data.put("candidateVotes", result.candidateVotes());
    data.put("computedAt", toTimestamp(result.computedAt()));
    data.put("checksum", result.checksum());
    return data;
  }

  @SuppressWarnings("unchecked")
  private BallotResult fromDocument(DocumentSnapshot doc) {
    Instant computedAt = doc.contains("computedAt")
        ? doc.getTimestamp("computedAt").toDate().toInstant()
        : Instant.EPOCH;
    Map<String, Long> candidateVotes;
    if (doc.contains("candidateVotes") && doc.get("candidateVotes") instanceof Map<?, ?> raw) {
      Map<String, Long> converted = new HashMap<>();
      raw.forEach((key, value) -> {
        if (key != null && value instanceof Number number) {
          converted.put(String.valueOf(key), number.longValue());
        }
      });
      candidateVotes = Map.copyOf(converted);
    } else {
      candidateVotes = Map.of();
    }
    return new BallotResult(
        doc.contains("id") ? doc.getString("id") : UUID.randomUUID().toString(),
        doc.getString("ballotId"),
        doc.getString("institutionId"),
        candidateVotes,
        computedAt,
        doc.getString("checksum")
    );
  }

  private Timestamp toTimestamp(Instant instant) {
    return Timestamp.ofTimeSecondsAndNanos(instant.getEpochSecond(), instant.getNano());
  }
}
