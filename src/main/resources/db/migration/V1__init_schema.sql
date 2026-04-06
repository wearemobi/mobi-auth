-- Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
-- V1: M.O.B.I. Core Schema & Tenant Fortress

-- User (Multi-tenant)
CREATE TABLE mobi_users (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    org_id UUID NOT NULL,
    tenant_id VARCHAR(100) NOT NULL,
    org_name VARCHAR(255) NOT NULL,
    password VARCHAR(255), -- Opcional/Nullable, ya que OCI (IdP) maneja las contraseñas
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Roles (One-to-Many para soportar múltiples roles por usuario)
CREATE TABLE mobi_user_roles (
     user_id UUID NOT NULL,
     role VARCHAR(50) NOT NULL,
     CONSTRAINT fk_mobi_user_roles_user_id FOREIGN KEY (user_id) REFERENCES mobi_users(id) ON DELETE CASCADE
);

-- Índices para búsquedas ultra rápidas de agentes
CREATE INDEX idx_mobi_users_tenant_id ON mobi_users(tenant_id);
CREATE INDEX idx_mobi_users_org_id ON mobi_users(org_id);


CREATE TABLE mobi_client (
      id UUID PRIMARY KEY,
      client_id VARCHAR(100) NOT NULL UNIQUE,
      client_secret_hash VARCHAR(255) NOT NULL,
      org_id UUID NOT NULL,
      tenant_id VARCHAR(100) NOT NULL,
      app_name VARCHAR(100) NOT NULL,
      created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_mobi_client_client_id ON mobi_client(client_id);
CREATE INDEX idx_mobi_client_tenant_id ON mobi_client(tenant_id);