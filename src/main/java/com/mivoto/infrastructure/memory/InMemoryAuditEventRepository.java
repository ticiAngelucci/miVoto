package com.mivoto.infrastructure.memory;

import com.mivoto.model.AuditEvent;
import com.mivoto.repository.AuditEventRepository;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

@Repository
@Profile("dev")
public class InMemoryAuditEventRepository implements AuditEventRepository {

  private final Map<String, AuditEvent> store = new ConcurrentHashMap<>();

  @Override
  public AuditEvent save(AuditEvent event) {
    String id = event.id() != null ? event.id() : UUID.randomUUID().toString();
    AuditEvent normalized = new AuditEvent(id, event.actor(), event.action(), event.metadata(), event.occurredAt());
    store.put(id, normalized);
    return normalized;
  }
}
