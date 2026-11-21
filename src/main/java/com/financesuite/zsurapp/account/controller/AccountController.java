
package com.financesuite.zsurapp.account.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.financesuite.zsurapp.account.dto.AccountResponse;
import com.financesuite.zsurapp.account.dto.CreateAccountRequest;
import com.financesuite.zsurapp.account.entity.Account;
import com.financesuite.zsurapp.account.service.AccountService;

import com.financesuite.zsurapp.account.dto.UpdateAccountRequest;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/accounts")
public class AccountController {

	private final AccountService service;

	public AccountController(AccountService service) {
		this.service = service;
	}

	@PostMapping
	public ResponseEntity<AccountResponse> create(@PathVariable UUID workspaceId,
			@Valid @RequestBody CreateAccountRequest request, @AuthenticationPrincipal Jwt jwt) {

		UUID ownerId = UUID.fromString(jwt.getSubject());

		Account account = service.create(workspaceId, request.name(), request.type(), request.currency(),
				request.initialBalance(), ownerId);

		URI location = URI.create("/api/v1/workspaces/" + workspaceId + "/accounts/" + account.getId());

		return ResponseEntity.created(location).body(AccountResponse.from(account));
	}

	@GetMapping
	public List<AccountResponse> findAll(@PathVariable UUID workspaceId, @AuthenticationPrincipal Jwt jwt) {

		UUID ownerId = UUID.fromString(jwt.getSubject());

		return service.findAllByWorkspace(workspaceId, ownerId).stream().map(AccountResponse::from).toList();
	}

	@GetMapping("/{accountId}")
	public AccountResponse findById(@PathVariable UUID workspaceId, @PathVariable UUID accountId,
			@AuthenticationPrincipal Jwt jwt) {

		UUID ownerId = UUID.fromString(jwt.getSubject());

		Account account = service.findById(workspaceId, accountId, ownerId);

		return AccountResponse.from(account);
	}

	@PatchMapping("/{accountId}")
	public AccountResponse rename(@PathVariable UUID workspaceId, @PathVariable UUID accountId,
			@Valid @RequestBody UpdateAccountRequest request, @AuthenticationPrincipal Jwt jwt) {

		UUID ownerId = UUID.fromString(jwt.getSubject());

		Account account = service.rename(workspaceId, accountId, request.name(), ownerId);

		return AccountResponse.from(account);
	}

}
