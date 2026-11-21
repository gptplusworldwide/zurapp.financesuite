
-- Finance Suite
-- Migración V1: infraestructura inicial

CREATE TABLE app_metadata (
    key VARCHAR(100) PRIMARY KEY,
    value VARCHAR(500) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);
