-- ========= PART 1: TABLE CREATION =========

-- Enable the pgcrypto extension to generate UUIDs if needed
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Create the 'tenants' table first, as 'logs' will reference it
CREATE TABLE tenants (
    id UUID PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL UNIQUE,
    api_token_hash VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL
);

-- Create the 'logs' table
CREATE TABLE logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(), -- Or let your app handle UUID generation
    ts TIMESTAMPTZ NOT NULL,
    message TEXT,
    service VARCHAR(100),
    level VARCHAR(50),
    tenant_id UUID NOT NULL,
    ingested_at TIMESTAMPTZ NOT NULL,
    hostname VARCHAR(255),
    client_ip VARCHAR(50),
    attrs JSONB,

    -- Define the foreign key relationship
    -- ON DELETE CASCADE means if a tenant is deleted, all their logs are automatically deleted too.
    CONSTRAINT fk_tenant
      FOREIGN KEY(tenant_id)
      REFERENCES tenants(id)
      ON DELETE CASCADE
);

-- ========= PART 2: INDEX CREATION =========

-- Create indexes on the 'logs' table for faster querying.
-- The most important index: for finding recent logs for a specific tenant quickly.
CREATE INDEX idx_logs_tenant_id_ts_desc ON logs (tenant_id, ts DESC);

-- Indexes for commonly filtered fields
CREATE INDEX idx_logs_service ON logs (service);
CREATE INDEX idx_logs_level ON logs (level);

-- A GIN index is specifically designed to make searching inside a JSONB column fast.
CREATE INDEX idx_logs_attrs_gin ON logs USING GIN (attrs);


-- ========= PART 3: INITIAL DATA INSERTION =========

-- Insert a sample tenant so you can start testing.
-- NOTE: In your real application, you will generate a secure hash of the API token.
-- For now, we'll use a placeholder.
INSERT INTO tenants (id, company_name, api_token_hash, created_at)
VALUES (
    'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11', -- This is your hardcoded tenant ID for testing
    'TestCorp Inc.',
    'placeholder_hashed_api_token', -- Replace with a real hash later
    NOW() -- Sets the creation time to the current time
);