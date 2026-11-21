
package com.financesuite.zsurapp.category.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

	@Id
	private UUID id;

	@Column(name = "workspace_id", nullable = false)
	private UUID workspaceId;

	@Column(nullable = false, length = 120)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private CategoryType type;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Category() {
	}

	public Category(UUID workspaceId, String name, CategoryType type) {

		if (workspaceId == null) {
			throw new IllegalArgumentException("El workspace es obligatorio");
		}

		if (name == null || name.isBlank() || name.trim().length() > 120) {
			throw new IllegalArgumentException("Nombre de categoria invalido");
		}

		if (type == null) {
			throw new IllegalArgumentException("El tipo de categoria es obligatorio");
		}

		this.id = UUID.randomUUID();
		this.workspaceId = workspaceId;
		this.name = name.trim();
		this.type = type;
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

	public CategoryType getType() {
		return type;
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
