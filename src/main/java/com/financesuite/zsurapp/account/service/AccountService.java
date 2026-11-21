
package com.financesuite.zsurapp.account.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.financesuite.zsurapp.account.entity.Account;
import com.financesuite.zsurapp.account.entity.AccountType;
import com.financesuite.zsurapp.account.repository.AccountRepository;
import com.financesuite.zsurapp.workspace.service.WorkspaceService;

@Service
public class AccountService {

	private final AccountRepository repository;
	private final WorkspaceService workspaceService;

	public AccountService(AccountRepository repository, WorkspaceService workspaceService) {

		this.repository = repository;
		this.workspaceService = workspaceService;
	}

	@Transactional
	public Account create(UUID workspaceId, String name, AccountType type, String currency, BigDecimal initialBalance,
			UUID ownerId) {

		// Confirma que el usuario puede acceder al Workspace.
		workspaceService.findByIdAndOwner(workspaceId, ownerId);

		Account account = new Account(workspaceId, name, type, currency, initialBalance);

		return repository.save(account);
	}

	@Transactional(readOnly = true)
	public List<Account> findAllByWorkspace(UUID workspaceId, UUID ownerId) {

		workspaceService.findByIdAndOwner(workspaceId, ownerId);

		return repository.findAllByWorkspaceId(workspaceId);
	}

	@Transactional(readOnly = true)
	public Account findById(UUID workspaceId, UUID accountId, UUID ownerId) {

		workspaceService.findByIdAndOwner(workspaceId, ownerId);

		return repository.findByIdAndWorkspaceId(accountId, workspaceId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta financiera no encontrada"));
	}

	@Transactional
	public Account rename(UUID workspaceId, UUID accountId, String newName, UUID ownerId) {

		// Primero verificamos el propietario del Workspace.
		workspaceService.findByIdAndOwner(workspaceId, ownerId);

		// Buscamos la cuenta dentro del Workspace autorizado.
		Account account = repository.findByIdAndWorkspaceId(accountId, workspaceId).orElseThrow(
				() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta financiera no encontrada"));

		// Actualizamos solamente el nombre.
		account.rename(newName);

		return account;
	}

}
