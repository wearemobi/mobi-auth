// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.event;

import com.wearemobi.auth.entity.UserEntity;

/**
 * Domain event published when a new user is successfully provisioned via JIT.
 */
public record UserRegisteredEvent(UserEntity user) {
}