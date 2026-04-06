-- User (Multi-tenant)
CREATE TABLE mobi_users (
                            id UUID PRIMARY KEY,
                            email VARCHAR(255) NOT NULL UNIQUE,
                            org_id UUID NOT NULL,
                            tenant_id VARCHAR(100) NOT NULL,
                            org_name VARCHAR(255) NOT NULL
);

-- Roles
CREATE TABLE mobi_user_roles (
                                 user_id UUID NOT NULL,
                                 role VARCHAR(50) NOT NULL,
                                 CONSTRAINT fk_mobi_user_roles_user_id FOREIGN KEY (user_id) REFERENCES mobi_users(id) ON DELETE CASCADE
);

-- Indexes
CREATE INDEX idx_mobi_users_tenant_id ON mobi_users(tenant_id);
CREATE INDEX idx_mobi_users_org_id ON mobi_users(org_id);