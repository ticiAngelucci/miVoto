package com.mivoto.infrastructure.firebase;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.Query;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.mivoto.model.VoteRecord;
import com.mivoto.repository.VoteRecordRepository;
import java.time.Instant;
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
public class FirestoreVoteRecordRepository implements VoteRecordRepository {

  private static final Logger log = LoggerFactory.getLogger(FirestoreVoteRecordRepository.class);
  private static final String COLLECTION = "votos";

  private final Firestore firestore;

  public FirestoreVoteRecordRepository(Firestore firestore) {
    this.firestore = Objects.requireNonNull(firestore);
  }

  @Override
  public VoteRecord save(VoteRecord record) {
    String id = record.id() != null ? record.id() : UUID.randomUUID().toString();
    DocumentReference doc = collection().document(id);
    try {
      doc.set(toDocument(record)).get();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to persist vote record", e);
    }
    return new VoteRecord(
        id,
        record.ballotId(),
        record.institutionId(),
        record.candidateIds(),
        record.voteHash(),
        record.tokenHash(),
        record.receipt(),
        record.txHash(),
        record.createdAt()
    );
  }

  @Override
  public Optional<VoteRecord> findByReceipt(String receipt) {
    try {
      Query query = collection().whereEqualTo("receipt", receipt).limit(1);
      QuerySnapshot snapshot = query.get().get();
      if (!snapshot.isEmpty()) {
        QueryDocumentSnapshot doc = snapshot.getDocuments().get(0);
        return Optional.of(fromDocument(doc));
      }
      return Optional.empty();
    } catch (Exception e) {
      log.error("Failed to find vote by receipt", e);
      return Optional.empty();
    }
  }

  @Override
  public Map<String, Long> tallyByBallot(String ballotId) {
    try {
      Query query = collection().whereEqualTo("ballotId", ballotId);
      List<QueryDocumentSnapshot> docs = query.get().get().getDocuments();
      Map<String, Long> counts = new HashMap<>();
      for (DocumentSnapshot doc : docs) {
        List<String> candidateIds = doc.contains("candidateIds")
            ? (List<String>) doc.get("candidateIds")
            : List.of();
        for (String candidateId : candidateIds) {
          counts.merge(candidateId, 1L, Long::sum);
        }
      }
      return counts;
    } catch (Exception e) {
      log.error("Failed to compute tally for ballot {}", ballotId, e);
      return Map.of();
    }
  }

  private CollectionReference collection() {
    return firestore.collection(COLLECTION);
  }

  private Map<String, Object> toDocument(VoteRecord record) {
    return Map.of(
        "ballotId", record.ballotId(),
        "institutionId", record.institutionId(),
        "voteHash", record.voteHash(),
        "tokenHash", record.tokenHash(),
        "receipt", record.receipt(),
        "txHash", record.txHash(),
        "candidateIds", record.candidateIds(),
        "createdAt", Timestamp.ofTimeSecondsAndNanos(record.createdAt().getEpochSecond(), record.createdAt().getNano())
    );
  }

  private VoteRecord fromDocument(DocumentSnapshot document) {
    Instant createdAt = document.contains("createdAt")
        ? document.getTimestamp("createdAt").toDate().toInstant()
        : Instant.EPOCH;
    List<String> candidateIds = document.contains("candidateIds")
        ? (List<String>) document.get("candidateIds")
        : List.of();
    return new VoteRecord(
        document.getId(),
        document.getString("ballotId"),
        document.getString("institutionId"),
        candidateIds,
        document.getString("voteHash"),
        document.getString("tokenHash"),
        document.getString("receipt"),
        document.getString("txHash"),
        createdAt
    );
  }
}
