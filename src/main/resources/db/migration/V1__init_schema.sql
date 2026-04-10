-- Copyright © 2026 M.O.B.I.™ (Machine Oriented Brilliant Ideas™)
-- V1: Oracle Core Schema - Multi-tenant Identity Provider

-- 1. Primary user entity (RAW(16) is the standard for UUID in Oracle for performance)
CREATE TABLE mobi_user
(
    id         RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    email      VARCHAR2(255) NOT NULL UNIQUE,
    org_id     RAW(16) NOT NULL,
    tenant_id  VARCHAR2(100) NOT NULL,
    org_name   VARCHAR2(255) NOT NULL,
    password   VARCHAR2(255), -- Managed by OCI (IdP)
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. User roles collection
CREATE TABLE mobi_user_role
(
    user_id RAW(16) NOT NULL,
    role    VARCHAR2(50) NOT NULL,
    CONSTRAINT fk_mobi_user_role_user_id FOREIGN KEY (user_id) REFERENCES mobi_user (id) ON DELETE CASCADE
);

-- 3. Machine-to-Machine (M2M) clients for agent authentication
CREATE TABLE mobi_client
(
    id                 RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    client_id          VARCHAR2(100) NOT NULL UNIQUE,
    client_secret_hash VARCHAR2(255) NOT NULL,
    org_id             RAW(16) NOT NULL,
    tenant_id          VARCHAR2(100) NOT NULL,
    app_name           VARCHAR2(100) NOT NULL,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. [ISSUE #25] The Audit Bunker (Using Oracle 23ai JSON support)
CREATE TABLE mobi_audit_log
(
    id              RAW(16) DEFAULT SYS_GUID() PRIMARY KEY,
    event_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    event_type      VARCHAR2(50) NOT NULL,
    principal_id    VARCHAR2(255),
    tenant_id       VARCHAR2(100),
    payload         JSON,
    source_ip       VARCHAR2(45)
);

-- 5. Strategic indexes for multi-tenant isolation
CREATE INDEX idx_mobi_user_tenant_id ON mobi_user (tenant_id);
CREATE INDEX idx_mobi_user_org_id ON mobi_user (org_id);
CREATE INDEX idx_mobi_client_client_id ON mobi_client (client_id);
CREATE INDEX idx_mobi_client_tenant_id ON mobi_client (tenant_id);
CREATE INDEX idx_mobi_audit_principal ON mobi_audit_log (principal_id);
