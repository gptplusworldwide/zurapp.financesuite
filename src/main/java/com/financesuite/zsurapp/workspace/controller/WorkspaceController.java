
package com.financesuite.zsurapp.workspace.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.financesuite.zsurapp.workspace.dto.CreateWorkspaceRequest;
import com.financesuite.zsurapp.workspace.dto.WorkspaceResponse;
import com.financesuite.zsurapp.workspace.entity.Workspace;
import com.financesuite.zsurapp.workspace.service.WorkspaceService;

import com.financesuite.zsurapp.workspace.dto.UpdateWorkspaceRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/workspaces")
@Validated
public class WorkspaceController {

	private final WorkspaceService service;

	public WorkspaceController(WorkspaceService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<WorkspaceResponse> create(@Valid @RequestBody CreateWorkspaceRequest request,
			@AuthenticationPrincipal Jwt jwt) {

		UUID ownerId = UUID.fromString(jwt.getSubject());

		Workspace workspace = service.create(request.name(), request.type(), ownerId);

		WorkspaceResponse response = WorkspaceResponse.from(workspace);

		return ResponseEntity.created(URI.create("/api/v1/workspaces/" + workspace.getId())).body(response);
	}

	@GetMapping
	public List<WorkspaceResponse> findAll(@AuthenticationPrincipal Jwt jwt) {

		UUID ownerId = UUID.fromString(jwt.getSubject());

		return service.findAllByOwner(ownerId).stream().map(WorkspaceResponse::from).toList();
	}

	@GetMapping("/{id}")
	public WorkspaceResponse findById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {

		UUID ownerId = UUID.fromString(jwt.getSubject());

		Workspace workspace = service.findByIdAndOwner(id, ownerId);

		return WorkspaceResponse.from(workspace);
	}

	@PatchMapping("/{id}")
	public WorkspaceResponse rename(@PathVariable UUID id, @Valid @RequestBody UpdateWorkspaceRequest request,
			@AuthenticationPrincipal Jwt jwt) {

		UUID ownerId = UUID.fromString(jwt.getSubject());

		Workspace workspace = service.rename(id, request.name(), ownerId);

		return WorkspaceResponse.from(workspace);
	}

}
