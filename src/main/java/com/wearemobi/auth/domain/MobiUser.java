/* Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™) */
package com.wearemobi.auth.domain;

import java.util.UUID;

/** Representación inmutable de un usuario en el ecosistema MOBI. */
public record MobiUser(UUID id, String email, String password, String role, String tenantId) {}
