
package com.financesuite.zsurapp.account.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.financesuite.zsurapp.account.dto.AccountBalanceResponse;
import com.financesuite.zsurapp.account.entity.Account;
import com.financesuite.zsurapp.account.repository.AccountRepository;
import com.financesuite.zsurapp.transaction.repository.TransactionRepository;
import com.financesuite.zsurapp.workspace.service.WorkspaceService;

@Service
public class AccountBalanceService {

	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;
	private final WorkspaceService workspaceService;

	public AccountBalanceService(AccountRepository accountRepository, TransactionRepository transactionRepository,
			WorkspaceService workspaceService) {

		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
		this.workspaceService = workspaceService;
	}

	@Transactional(readOnly = true)
	public AccountBalanceResponse getBalance(UUID workspaceId, UUID accountId, UUID ownerId) {

		// 1. Comprobar que el Workspace pertenece al usuario.
		workspaceService.findByIdAndOwner(workspaceId, ownerId);

		// 2. Buscar la cuenta dentro del Workspace autorizado.
		Account account = accountRepository.findByIdAndWorkspaceId(accountId, workspaceId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada"));

		// 3. Consultar los ingresos y gastos.
		BigDecimal totalIncome = transactionRepository.sumIncomeByAccount(workspaceId, accountId);

		BigDecimal totalExpense = transactionRepository.sumExpenseByAccount(workspaceId, accountId);

		// 4. Calcular el saldo sin modificar initialBalance.
		BigDecimal currentBalance = account.getInitialBalance().add(totalIncome).subtract(totalExpense);

		// 5. Preparar la respuesta.
		return new AccountBalanceResponse(account.getId(), workspaceId, account.getName(), account.getCurrency(),
				account.getInitialBalance(), totalIncome, totalExpense, currentBalance);
	}
}
