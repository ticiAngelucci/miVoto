package com.mivoto.model;

import java.time.Instant;
import java.util.Map;

public record AuditEvent(
    String id,
    String actor,
    String action,
    Map<String, Object> metadata,
    Instant occurredAt
) {
}
