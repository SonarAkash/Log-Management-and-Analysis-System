DROP TABLE IF EXISTS _user CASCADE;
DROP TABLE IF EXISTS logs CASCADE;
DROP TABLE IF EXISTS tenants CASCADE;


CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL UNIQUE,
    api_token_hash VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);


CREATE TABLE _user (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    tenant_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tenant
      FOREIGN KEY(tenant_id)
      REFERENCES tenants(id)
      ON DELETE CASCADE
);

CREATE INDEX idx_user_tenant_id ON _user (tenant_id);

CREATE TABLE logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    ts TIMESTAMPTZ NOT NULL,
    message TEXT,
    service VARCHAR(100),
    level VARCHAR(50),
    tenant_id UUID NOT NULL,
    ingested_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    hostname VARCHAR(255),
    client_ip VARCHAR(50),
    attrs JSONB,
    message_tsv tsvector GENERATED ALWAYS AS (to_tsvector('english', message)) STORED,
    CONSTRAINT fk_tenant
      FOREIGN KEY(tenant_id)
      REFERENCES tenants(id)
      ON DELETE CASCADE
);





CREATE INDEX idx_logs_tenant_id_ts_desc ON logs (tenant_id, ts DESC);
CREATE INDEX idx_logs_service ON logs (service);
CREATE INDEX idx_logs_level ON logs (level);
CREATE INDEX idx_logs_hostname ON logs (hostname);
CREATE INDEX idx_logs_attrs_gin ON logs USING GIN (attrs);
CREATE INDEX idx_logs_message_tsv_gin ON logs USING GIN (message_tsv);
CREATE INDEX idx_logs_attrs_fts ON logs USING GIN (jsonb_to_tsvector('english', attrs, '["string"]'));

