
CREATE TABLE accounts (
    id UUID PRIMARY KEY,

    workspace_id UUID NOT NULL,

    name VARCHAR(120) NOT NULL,

    type VARCHAR(30) NOT NULL,

    currency VARCHAR(3) NOT NULL,

    initial_balance NUMERIC(19, 4) NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_accounts_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id),

    CONSTRAINT chk_account_name
        CHECK (length(trim(name)) > 0),

    CONSTRAINT chk_account_type
        CHECK (type IN (
            'CASH',
            'BANK',
            'DIGITAL_WALLET'
        )),

    CONSTRAINT chk_account_currency
        CHECK (currency ~ '^[A-Z]{3}$')
);

CREATE INDEX idx_accounts_workspace_id
    ON accounts(workspace_id);

CREATE INDEX idx_accounts_workspace_name
    ON accounts(workspace_id, name);
