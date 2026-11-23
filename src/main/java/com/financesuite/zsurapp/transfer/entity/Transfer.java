
package com.financesuite.zsurapp.transfer.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "transfers")
public class Transfer {

	@Id
	private UUID id;

	@Column(name = "workspace_id", nullable = false)
	private UUID workspaceId;

	@Column(name = "from_account_id", nullable = false)
	private UUID fromAccountId;

	@Column(name = "to_account_id", nullable = false)
	private UUID toAccountId;

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

	protected Transfer() {
	}

	public Transfer(UUID workspaceId, UUID fromAccountId, UUID toAccountId, BigDecimal amount, String description,
			Instant occurredAt) {

		if (workspaceId == null) {
			throw new IllegalArgumentException("El Workspace es obligatorio");
		}

		if (fromAccountId == null || toAccountId == null) {
			throw new IllegalArgumentException("Las cuentas son obligatorias");
		}

		if (fromAccountId.equals(toAccountId)) {
			throw new IllegalArgumentException("Las cuentas deben ser diferentes");
		}

		if (amount == null || amount.signum() <= 0 || amount.scale() > 4 || amount.precision() > 19) {
			throw new IllegalArgumentException("Importe invalido");
		}

		if (occurredAt == null) {
			throw new IllegalArgumentException("La fecha es obligatoria");
		}

		if (description != null && description.trim().length() > 500) {
			throw new IllegalArgumentException("La descripcion supera los 500 caracteres");
		}

		this.id = UUID.randomUUID();
		this.workspaceId = workspaceId;
		this.fromAccountId = fromAccountId;
		this.toAccountId = toAccountId;
		this.amount = amount;
		this.description = description == null ? null : description.trim();
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

	public UUID getFromAccountId() {
		return fromAccountId;
	}

	public UUID getToAccountId() {
		return toAccountId;
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