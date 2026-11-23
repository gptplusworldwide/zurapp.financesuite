
package com.financesuite.zsurapp.transaction.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    private UUID id;

    @Column(name = "workspace_id", nullable = false)
    private UUID workspaceId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransactionType type;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(length = 500)
    private String description;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Transaction() {
        // Requerido por JPA
    }

    public Transaction(
            UUID workspaceId,
            UUID accountId,
            UUID categoryId,
            TransactionType type,
            BigDecimal amount,
            String description,
            Instant occurredAt) {

        if (workspaceId == null ||
                accountId == null ||
                categoryId == null) {
            throw new IllegalArgumentException(
                "Workspace, cuenta y categoria son obligatorios"
            );
        }

        if (type == null) {
            throw new IllegalArgumentException(
                "El tipo es obligatorio"
            );
        }

        if (amount == null ||
                amount.signum() <= 0 ||
                amount.scale() > 4 ||
                amount.precision() > 19) {
            throw new IllegalArgumentException(
                "El importe debe ser positivo y tener precision NUMERIC(19,4)"
            );
        }

        if (occurredAt == null) {
            throw new IllegalArgumentException(
                "La fecha de la transaccion es obligatoria"
            );
        }

        if (description != null &&
                description.trim().length() > 500) {
            throw new IllegalArgumentException(
                "La descripcion no puede superar los 500 caracteres"
            );
        }

        this.id = UUID.randomUUID();
        this.workspaceId = workspaceId;
        this.accountId = accountId;
        this.categoryId = categoryId;
        this.type = type;
        this.amount = amount;
        this.description = description == null
                ? null
                : description.trim();
        this.occurredAt = occurredAt;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getWorkspaceId() {
        return workspaceId;
    }

    public UUID getAccountId() {
        return accountId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public TransactionType getType() {
        return type;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }

    public Instant getOccurredAt() {
        return occurredAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
