// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.component;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class TokenSensor {

  private static final Logger log = LoggerFactory.getLogger(TokenSensor.class);
  private final PasswordEncoder passwordEncoder;

  @Value("${mobi.debug.sensors:false}")
  private boolean enabled;

  public TokenSensor(PasswordEncoder passwordEncoder) {
    this.passwordEncoder = passwordEncoder;
  }

  /** Prints a debug message only if the Franky Switch is ON. */
  public void debug(String message, Object... args) {
    if (enabled) {
      log.info("🛠️ [TokenSensor:debug] > " + message, args);
    }
  }

  /** Special sensor to inspect hashes and compare with raw secrets. */
  public void inspectAuth(String raw, String storedHash, String label) {
    if (enabled) {
      log.info("🛠️ [TokenSensor: {}]", label);
      log.info("  > Raw Secret: [{}]", raw);
      log.info("  > Stored Hash: [{}]", storedHash);
      log.info("  > Matches? -> {}", passwordEncoder.matches(raw, storedHash));
      // Generamos un hash fresco para comparar
      log.info("  > New hash for DB: [{}]", passwordEncoder.encode(raw));
    }
  }
}
