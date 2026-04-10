// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.component;

import com.wearemobi.auth.entity.AuditLogEntity;
import com.wearemobi.auth.event.UserRegisteredEvent;
import com.wearemobi.auth.repository.AuditLogRepository;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class AuditLogListener {
  private static final Logger log = LoggerFactory.getLogger(AuditLogListener.class);

  private final AuditLogRepository auditRepository;

  public AuditLogListener(AuditLogRepository auditRepository) {
    this.auditRepository = auditRepository;
  }

  @EventListener
  @Async
  public void handleUserRegistered(UserRegisteredEvent event) {
    final AuditLogEntity auditLog = new AuditLogEntity();
    auditLog.setEventType("USER_REGISTERED");
    auditLog.setPrincipalId(event.user().getEmail());
    auditLog.setTenantId(event.user().getTenantId());
    auditLog.setPayload(
        Map.of(
            "email", event.user().getEmail(),
            "org_name", event.user().getOrgName(),
            "action", "OCI_PROVISIONING_SUCCESS"));

    auditRepository.save(auditLog);
    log.info("Used Registered event for user: {}", event.user().getEmail());
  }
}
