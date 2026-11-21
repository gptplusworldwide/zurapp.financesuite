
package com.financesuite.zsurapp.account.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
public class Account {

	@Id
	private UUID id;

	@Column(name = "workspace_id", nullable = false)
	private UUID workspaceId;

	@Column(nullable = false, length = 120)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private AccountType type;

	@Column(nullable = false, length = 3)
	private String currency;

	@Column(name = "initial_balance", nullable = false, precision = 19, scale = 4)
	private BigDecimal initialBalance;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Account() {
	}

	public Account(UUID workspaceId, String name, AccountType type, String currency, BigDecimal initialBalance) {

		if (workspaceId == null) {
			throw new IllegalArgumentException("El workspace es obligatorio");
		}

		if (name == null || name.isBlank() || name.trim().length() > 120) {
			throw new IllegalArgumentException("Nombre de cuenta invalido");
		}

		if (type == null) {
			throw new IllegalArgumentException("El tipo es obligatorio");
		}

		if (currency == null || !currency.matches("[A-Z]{3}")) {
			throw new IllegalArgumentException("Codigo de moneda invalido");
		}

		if (initialBalance == null) {
			throw new IllegalArgumentException("El saldo inicial es obligatorio");
		}

		if (initialBalance.scale() > 4 || initialBalance.precision() - initialBalance.scale() > 15) {
			throw new IllegalArgumentException("El saldo excede la precision permitida");
		}

		this.id = UUID.randomUUID();
		this.workspaceId = workspaceId;
		this.name = name.trim();
		this.type = type;
		this.currency = currency;
		this.initialBalance = initialBalance;
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

	public String getName() {
		return name;
	}

	public AccountType getType() {
		return type;
	}

	public String getCurrency() {
		return currency;
	}

	public BigDecimal getInitialBalance() {
		return initialBalance;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

	public void rename(String newName) {

		if (newName == null || newName.isBlank()) {
			throw new IllegalArgumentException("El nombre es obligatorio");
		}

		String normalizedName = newName.trim();

		if (normalizedName.length() > 120) {
			throw new IllegalArgumentException("El nombre no puede superar los 120 caracteres");
		}

		this.name = normalizedName;
	}

}
