
package com.financesuite.zsurapp.workspace.entity;

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
@Table(name = "workspaces")
public class Workspace {

	@Id
	@Column(name = "id", nullable = false)
	private UUID id;

	@Column(name = "name", nullable = false, length = 120)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 20)
	private WorkspaceType type;

	@Column(name = "owner_id", nullable = false)
	private UUID ownerId;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	protected Workspace() {
		// Constructor requerido por JPA
	}

	public Workspace(String name, WorkspaceType type, UUID ownerId) {

		this.id = UUID.randomUUID();
		this.name = name;
		this.type = type;
		this.ownerId = ownerId;
	}

	@PrePersist
	protected void onCreate() {
		Instant now = Instant.now();

		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	protected void onUpdate() {
		this.updatedAt = Instant.now();
	}

	public UUID getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public WorkspaceType getType() {
		return type;
	}

	public UUID getOwnerId() {
		return ownerId;
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
