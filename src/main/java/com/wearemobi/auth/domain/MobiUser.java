// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.domain;

import java.util.UUID;

/** Immutable representation of a user in the MOBI ecosystem. */
public record MobiUser(
    UUID id,
    String email,
    String password,
    String role,
    String tenantId,
    String orgId,
    String orgName) {}
