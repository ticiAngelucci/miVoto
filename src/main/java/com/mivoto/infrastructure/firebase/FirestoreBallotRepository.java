package com.mivoto.infrastructure.firebase;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.mivoto.model.Ballot;
import com.mivoto.repository.BallotRepository;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("!dev")
public class FirestoreBallotRepository implements BallotRepository {

  private static final Logger log = LoggerFactory.getLogger(FirestoreBallotRepository.class);
  private static final String COLLECTION = "boletas";
  private final Firestore firestore;

  public FirestoreBallotRepository(Firestore firestore) {
    this.firestore = Objects.requireNonNull(firestore);
  }

  @Override
  public Optional<Ballot> findById(String id) {
    try {
      DocumentSnapshot snapshot = firestore.collection(COLLECTION).document(id).get().get();
      if (!snapshot.exists()) {
        return Optional.empty();
      }
      return Optional.of(fromDocument(snapshot));
    } catch (Exception e) {
      log.error("Failed to load ballot {}", id, e);
      return Optional.empty();
    }
  }

  @Override
  public List<Ballot> findAll() {
    try {
      QuerySnapshot snapshot = firestore.collection(COLLECTION).get().get();
      List<Ballot> ballots = new ArrayList<>();
      for (QueryDocumentSnapshot document : snapshot.getDocuments()) {
        ballots.add(fromDocument(document));
      }
      return ballots;
    } catch (Exception e) {
      log.error("Failed to list ballots", e);
      return List.of();
    }
  }

  private Ballot fromDocument(DocumentSnapshot document) {
    Instant opensAt = document.contains("opensAt") ? toInstant(document.getTimestamp("opensAt")) : null;
    Instant closesAt = document.contains("closesAt") ? toInstant(document.getTimestamp("closesAt")) : null;
    List<String> candidateIds = document.contains("candidateIds")
        ? (List<String>) document.get("candidateIds")
        : List.of();
    boolean multiple = Boolean.TRUE.equals(document.getBoolean("allowMultipleSelection"));
    return new Ballot(
        document.getId(),
        document.getString("institutionId"),
        document.getString("title"),
        candidateIds,
        opensAt,
        closesAt,
        multiple
    );
  }

  private Instant toInstant(Timestamp timestamp) {
    return timestamp == null ? null : timestamp.toDate().toInstant();
  }
}
