
package com.financesuite.zsurapp.account.controller;

import java.util.UUID;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financesuite.zsurapp.account.dto.AccountBalanceResponse;
import com.financesuite.zsurapp.account.service.AccountBalanceService;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/accounts/{accountId}/balance")
public class AccountBalanceController {

	private final AccountBalanceService service;

	public AccountBalanceController(AccountBalanceService service) {
		this.service = service;
	}

	@GetMapping
	public AccountBalanceResponse getBalance(@PathVariable UUID workspaceId, @PathVariable UUID accountId,
			@AuthenticationPrincipal Jwt jwt) {

		UUID ownerId = UUID.fromString(jwt.getSubject());

		return service.getBalance(workspaceId, accountId, ownerId);
	}
}
