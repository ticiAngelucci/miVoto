package com.mivoto.domain.repository;

import com.mivoto.domain.model.AuditEvent;

public interface AuditEventRepository {

  AuditEvent save(AuditEvent event);
}
