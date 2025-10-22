package com.mivoto.service.audit;

import com.mivoto.model.AuditEvent;
import com.mivoto.repository.AuditEventRepository;
import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

  private final AuditEventRepository repository;
  private final Clock clock;

  public AuditService(AuditEventRepository repository, Clock clock) {
    this.repository = Objects.requireNonNull(repository);
    this.clock = Objects.requireNonNull(clock);
  }

  public AuditEvent record(String actor, String action, Map<String, Object> metadata) {
    AuditEvent event = new AuditEvent(UUID.randomUUID().toString(), actor, action, metadata, Instant.now(clock));
    return repository.save(event);
  }
}
