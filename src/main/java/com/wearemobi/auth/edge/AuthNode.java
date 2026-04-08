// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.edge;

import java.util.List;

public record AuthNode(
    String uid,
    String username,
    String name,
    String email,
    String tenantId,
    String orgId,
    String orgName,
    List<String> roles,
    String status) {}
