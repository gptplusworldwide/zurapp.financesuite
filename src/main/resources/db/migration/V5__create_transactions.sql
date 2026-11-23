
CREATE TABLE transactions (
    id UUID PRIMARY KEY,

    workspace_id UUID NOT NULL,
    account_id UUID NOT NULL,
    category_id UUID NOT NULL,

    type VARCHAR(20) NOT NULL,
    amount NUMERIC(19,4) NOT NULL,

    description VARCHAR(500),
    occurred_at TIMESTAMPTZ NOT NULL,

    created_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL
        DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_transactions_workspace
        FOREIGN KEY (workspace_id)
        REFERENCES workspaces(id),

    CONSTRAINT fk_transactions_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id),

    CONSTRAINT fk_transactions_category
        FOREIGN KEY (category_id)
        REFERENCES categories(id),

    CONSTRAINT chk_transaction_type
        CHECK (type IN ('INCOME', 'EXPENSE')),

    CONSTRAINT chk_transaction_amount
        CHECK (amount > 0)
);

CREATE INDEX idx_transactions_workspace_id
    ON transactions(workspace_id);

CREATE INDEX idx_transactions_account_id
    ON transactions(account_id);

CREATE INDEX idx_transactions_category_id
    ON transactions(category_id);

CREATE INDEX idx_transactions_occurred_at
    ON transactions(occurred_at);
