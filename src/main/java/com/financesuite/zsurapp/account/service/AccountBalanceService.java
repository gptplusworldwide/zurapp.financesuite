
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
import com.financesuite.zsurapp.transfer.repository.TransferRepository;
import com.financesuite.zsurapp.workspace.service.WorkspaceService;

@Service
public class AccountBalanceService {

	private final AccountRepository accountRepository;
	private final TransactionRepository transactionRepository;
	private final TransferRepository transferRepository;
	private final WorkspaceService workspaceService;

	public AccountBalanceService(AccountRepository accountRepository, TransactionRepository transactionRepository,
			TransferRepository transferRepository, WorkspaceService workspaceService) {

		this.accountRepository = accountRepository;
		this.transactionRepository = transactionRepository;
		this.transferRepository = transferRepository;
		this.workspaceService = workspaceService;
	}

	@Transactional(readOnly = true)
	public AccountBalanceResponse getBalance(UUID workspaceId, UUID accountId, UUID ownerId) {

		// 1. Validar que el Workspace pertenece al usuario.
		workspaceService.findByIdAndOwner(workspaceId, ownerId);

		// 2. Buscar la cuenta dentro del Workspace.
		Account account = accountRepository.findByIdAndWorkspaceId(accountId, workspaceId)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cuenta no encontrada"));

		// 3. Calcular ingresos.
		BigDecimal totalIncome = transactionRepository.sumIncomeByAccount(workspaceId, accountId);

		// 4. Calcular gastos.
		BigDecimal totalExpense = transactionRepository.sumExpenseByAccount(workspaceId, accountId);

		// 5. Calcular transferencias recibidas.
		BigDecimal incomingTransfers = transferRepository.sumIncomingByAccount(workspaceId, accountId);

		// 6. Calcular transferencias enviadas.
		BigDecimal outgoingTransfers = transferRepository.sumOutgoingByAccount(workspaceId, accountId);

		// 7. Calcular el saldo actual.
		BigDecimal currentBalance = account.getInitialBalance().add(totalIncome).subtract(totalExpense)
				.add(incomingTransfers).subtract(outgoingTransfers);

		// 8. Devolver el saldo sin modificar initialBalance.

		return new AccountBalanceResponse(account.getId(), workspaceId, account.getName(), account.getCurrency(),
				account.getInitialBalance(), totalIncome, totalExpense, incomingTransfers, outgoingTransfers,
				currentBalance);

	}
}
