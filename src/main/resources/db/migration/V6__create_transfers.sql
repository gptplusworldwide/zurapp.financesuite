
CREATE TABLE transfers (
    id UUID PRIMARY KEY,

    workspace_id UUID NOT NULL,
    from_account_id UUID NOT NULL,
    to_account_id UUID NOT NULL,

    amount NUMERIC(19,4) NOT NULL,
    description VARCHAR(500),

    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transfers_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id),

    CONSTRAINT fk_transfers_from_account
        FOREIGN KEY (from_account_id)
        REFERENCES accounts(id),

    CONSTRAINT fk_transfers_to_account
        FOREIGN KEY (to_account_id)
        REFERENCES accounts(id),

    CONSTRAINT chk_transfers_amount
        CHECK (amount > 0),

    CONSTRAINT chk_transfers_distinct_accounts
        CHECK (from_account_id <> to_account_id)
);

CREATE INDEX idx_transfers_workspace_id
    ON transfers(workspace_id);

CREATE INDEX idx_transfers_from_account_id
    ON transfers(from_account_id);

CREATE INDEX idx_transfers_to_account_id
    ON transfers(to_account_id);

CREATE INDEX idx_transfers_occurred_at
    ON transfers(occurred_at);
