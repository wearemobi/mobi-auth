// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.mapper;

import com.wearemobi.auth.domain.MobiUser;
import com.wearemobi.auth.domain.Role;
import com.wearemobi.auth.entity.UserEntity;

public final class UserMapper {

  private UserMapper() {
    // Utility class, prevent instantiation
  }

  public static MobiUser toDomain(UserEntity entity) {
    // Java 21 + Streams: Extract the primary role securely for the current MVP Record
    var primaryRole =
        entity.getRoles().stream().findFirst().map(Role::name).orElse("MOBI_TENANT_USER");

    return new MobiUser(
        entity.getId(),
        entity.getEmail(),
        null, // No need to transit password hashes in memory unless required
        primaryRole,
        entity.getTenantId(),
        entity.getOrgId().toString(),
        entity.getOrgName());
  }
}
