// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.domain;

/** M.O.B.I.™ Role Hierarchy Professional-grade permissions for the ecosystem. */
public enum Role {
  MOBI_CORE_ADMIN,
  MOBI_TENANT_OWNER,
  MOBI_TENANT_USER,
  MOBI_TENANT_CLIENT,
  MOBI_SYSTEM_AGENT;

  // Helper for Spring Security naming convention
  public String getAuthority() {
    return "ROLE_" + this.name();
  }
}
