// Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
package com.wearemobi.auth.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "mobi_client")
public class ClientEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "client_id", unique = true, nullable = false)
  private String clientId;

  @Column(name = "client_secret_hash", nullable = false)
  private String clientSecretHash;

  @Column(name = "org_id", nullable = false)
  private UUID orgId;

  @Column(name = "tenant_id", nullable = false)
  private String tenantId;

  @Column(name = "app_name", nullable = false)
  private String appName;

  // --- Constructor, Getters y Setters ---
  public ClientEntity() {}

  public UUID getId() {
    return id;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public String getClientSecretHash() {
    return clientSecretHash;
  }

  public void setClientSecretHash(String hash) {
    this.clientSecretHash = hash;
  }

  public UUID getOrgId() {
    return orgId;
  }

  public void setOrgId(UUID orgId) {
    this.orgId = orgId;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }
}
