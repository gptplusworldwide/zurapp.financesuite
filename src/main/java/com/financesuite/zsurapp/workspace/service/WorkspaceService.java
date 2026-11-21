
package com.financesuite.zsurapp.workspace.service;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financesuite.zsurapp.workspace.entity.Workspace;
import com.financesuite.zsurapp.workspace.entity.WorkspaceType;
import com.financesuite.zsurapp.workspace.repository.WorkspaceRepository;

import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WorkspaceService {

	private final WorkspaceRepository repository;

	public WorkspaceService(WorkspaceRepository repository) {
		this.repository = repository;
	}

	@Transactional(readOnly = true)
	public List<Workspace> findAllByOwner(UUID ownerId) {
		return repository.findAllByOwnerId(ownerId);
	}

	@Transactional
	public Workspace create(String name, WorkspaceType type, UUID ownerId) {

		if (name == null || name.isBlank()) {
			throw new IllegalArgumentException("El nombre del espacio es obligatorio");
		}

		if (name.length() > 120) {
			throw new IllegalArgumentException("El nombre no puede superar los 120 caracteres");
		}

		if (type == null) {
			throw new IllegalArgumentException("El tipo del espacio es obligatorio");
		}

		if (ownerId == null) {
			throw new IllegalArgumentException("El propietario es obligatorio");
		}

		Workspace workspace = new Workspace(name.trim(), type, ownerId);

		return repository.save(workspace);
	}

	@Transactional(readOnly = true)
	public Workspace findByIdAndOwner(UUID workspaceId, UUID ownerId) {

		return repository.findByIdAndOwnerId(workspaceId, ownerId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Espacio financiero no encontrado"));
	}

	@Transactional
	public Workspace rename(UUID workspaceId, String newName, UUID ownerId) {

		Workspace workspace = repository.findByIdAndOwnerId(workspaceId, ownerId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Espacio financiero no encontrado"));

		workspace.rename(newName);

		return workspace;
	}

}
