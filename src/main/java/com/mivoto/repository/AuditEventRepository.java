package com.mivoto.repository;

import com.mivoto.model.AuditEvent;

public interface AuditEventRepository {

  AuditEvent save(AuditEvent event);
}
