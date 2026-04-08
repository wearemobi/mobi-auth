// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.mapper;

import com.wearemobi.auth.domain.*;
import com.wearemobi.auth.edge.*;
import com.wearemobi.auth.entity.UserEntity;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class EdgeMapper {

  private EdgeMapper() {}

  /** Forja el Poneglyph v1.9 para enviar a Cloudflare KV. */
  public static EdgeProfile toEdgeProfile(UserEntity user /*, BioEntity bio */) {

    // 1. Mapeo de Roles a formato "ROLE_..."
    List<String> roleStrings =
        user.getRoles().stream().map(Role::getAuthority).collect(Collectors.toList());

    // 2. Construir el nodo Auth (Seguridad y Verdad)
    AuthNode auth =
        new AuthNode(
            user.getId().toString(), // uid
            "slug", // user.getSlug(),                // username (duplicado por seguridad)
            "name", // user.getName(),                // full name
            user.getEmail(), // email
            user.getTenantId(), // tenantId
            user.getOrgId().toString(), // orgId
            user.getOrgName(), // orgName
            roleStrings, // roles
            "ACTIVE" // status por defecto (MVP)
            );

    // 3. Construir el nodo Bio (Mapeado desde BioEntity en el futuro)
    // [TODO]: Reemplazar estos mock-datos con los datos reales de BioEntity
    SignatureNode signature =
        new SignatureNode(
            "name", // user.getName(), // Por ahora usamos el legal, luego el de la Bio
            "M.O.B.I.™ Officer",
            "https://wearemobi.com/icon-light.svg",
            "# Identity, Refined...",
            "## Welcome to my edge profile.");
    BioNode bioNode = new BioNode(signature, Collections.emptyMap());

    // 4. Construir Metadata (Súper-extensible)
    MetadataNode metadata =
        new MetadataNode("PRO", Instant.now().toString(), "dark", List.of("agentic_ready"));

    // 5. Ensamblaje Final SÚPER
    return new EdgeProfile(
        "slug", // user.getSlug(),
        auth, bioNode, metadata);
  }
}
