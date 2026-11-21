
CREATE TABLE categories (
    id UUID PRIMARY KEY,

    workspace_id UUID NOT NULL,

    name VARCHAR(120) NOT NULL,

    type VARCHAR(20) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_categories_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id),

    CONSTRAINT chk_category_name
        CHECK (length(trim(name)) > 0),

    CONSTRAINT chk_category_type
        CHECK (type IN ('INCOME', 'EXPENSE')),

    CONSTRAINT uq_category_workspace_type_name
        UNIQUE (workspace_id, type, name)
);

CREATE INDEX idx_categories_workspace_id
    ON categories(workspace_id);
