package com.mivoto.infra.firebase;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.DocumentReference;
import com.google.cloud.firestore.Firestore;
import com.mivoto.domain.model.AuditEvent;
import com.mivoto.domain.repository.AuditEventRepository;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Repository;

@Repository
public class FirestoreAuditEventRepository implements AuditEventRepository {

  private final Firestore firestore;

  public FirestoreAuditEventRepository(Firestore firestore) {
    this.firestore = Objects.requireNonNull(firestore);
  }

  @Override
  public AuditEvent save(AuditEvent event) {
    String id = event.id() != null ? event.id() : UUID.randomUUID().toString();
    DocumentReference doc = firestore.collection("audit_events").document(id);
    try {
      doc.set(Map.of(
          "actor", event.actor(),
          "action", event.action(),
          "metadata", event.metadata(),
          "occurredAt", Timestamp.ofTimeSecondsAndNanos(event.occurredAt().getEpochSecond(), event.occurredAt().getNano())
      )).get();
    } catch (Exception e) {
      throw new IllegalStateException("Failed to persist audit event", e);
    }
    return new AuditEvent(id, event.actor(), event.action(), event.metadata(), event.occurredAt());
  }
}
