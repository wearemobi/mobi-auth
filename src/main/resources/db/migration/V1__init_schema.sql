-- Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
-- V1: Core Schema and Multi-tenant Identity Provider

-- Primary user entity for multi-tenant authentication
CREATE TABLE mobi_user (
                           id UUID PRIMARY KEY,
                           email VARCHAR(255) NOT NULL UNIQUE,
                           org_id UUID NOT NULL,
                           tenant_id VARCHAR(100) NOT NULL,
                           org_name VARCHAR(255) NOT NULL,
                           password VARCHAR(255), -- Nullable: Human credentials managed by OCI (IdP)
                           created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- User roles collection (One-to-Many relationship)
CREATE TABLE mobi_user_role (
                                user_id UUID NOT NULL,
                                role VARCHAR(50) NOT NULL,
                                CONSTRAINT fk_mobi_user_role_user_id FOREIGN KEY (user_id) REFERENCES mobi_user(id) ON DELETE CASCADE
);

-- Machine-to-Machine (M2M) clients for agent authentication
CREATE TABLE mobi_client (
                             id UUID PRIMARY KEY,
                             client_id VARCHAR(100) NOT NULL UNIQUE,
                             client_secret_hash VARCHAR(255) NOT NULL,
                             org_id UUID NOT NULL,
                             tenant_id VARCHAR(100) NOT NULL,
                             app_name VARCHAR(100) NOT NULL,
                             created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Strategic indexes for multi-tenant isolation and lookup performance
CREATE INDEX idx_mobi_user_tenant_id ON mobi_user(tenant_id);
CREATE INDEX idx_mobi_user_org_id ON mobi_user(org_id);
CREATE INDEX idx_mobi_client_client_id ON mobi_client(client_id);
CREATE INDEX idx_mobi_client_tenant_id ON mobi_client(tenant_id);

