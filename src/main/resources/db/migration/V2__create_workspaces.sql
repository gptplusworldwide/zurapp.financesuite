
CREATE TABLE workspaces (
    id UUID PRIMARY KEY,

    name VARCHAR(120) NOT NULL,

    type VARCHAR(20) NOT NULL,

    owner_id UUID NOT NULL,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT chk_workspace_type
        CHECK (type IN ('PERSONAL', 'BUSINESS')),

    CONSTRAINT chk_workspace_name
        CHECK (length(trim(name)) > 0)
);

CREATE INDEX idx_workspaces_owner_id
    ON workspaces(owner_id);
