// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.hook;

import com.wearemobi.auth.event.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class PostRegistrationHook {

  private static final Logger log = LoggerFactory.getLogger(PostRegistrationHook.class);

  @EventListener
  public void handleUserRegistration(UserRegisteredEvent event) {
    // 🛠️ DEBUG: Franky's sensor triggered for user registration
    log.info("Post-registration hook triggered for user: {}", event.user().getEmail());
    log.info("Tenant provisioning pending for: {}", event.user().getTenantId());
  }
}
