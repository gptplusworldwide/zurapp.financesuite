
package com.financesuite.zsurapp.account.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.financesuite.zsurapp.account.dto.AccountBalanceResponse;
import com.financesuite.zsurapp.account.entity.Account;
import com.financesuite.zsurapp.account.repository.AccountRepository;
import com.financesuite.zsurapp.transaction.repository.TransactionRepository;
import com.financesuite.zsurapp.workspace.service.WorkspaceService;

@ExtendWith(MockitoExtension.class)
class AccountBalanceServiceTest {

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private TransactionRepository transactionRepository;

	@Mock
	private WorkspaceService workspaceService;

	private AccountBalanceService service;

	private UUID workspaceId;
	private UUID accountId;
	private UUID ownerId;

	@BeforeEach
	void setUp() {
		service = new AccountBalanceService(accountRepository, transactionRepository, workspaceService);

		workspaceId = UUID.randomUUID();
		accountId = UUID.randomUUID();
		ownerId = UUID.randomUUID();
	}

	@Test
	void shouldCalculateBalanceWithIncomeAndExpense() {

		Account account = mock(Account.class);

		when(accountRepository.findByIdAndWorkspaceId(accountId, workspaceId)).thenReturn(Optional.of(account));

		when(account.getId()).thenReturn(accountId);
		when(account.getName()).thenReturn("Efectivo personal");
		when(account.getCurrency()).thenReturn("ARS");
		when(account.getInitialBalance()).thenReturn(new BigDecimal("150000.0000"));

		when(transactionRepository.sumIncomeByAccount(workspaceId, accountId)).thenReturn(new BigDecimal("50000.0000"));

		when(transactionRepository.sumExpenseByAccount(workspaceId, accountId))
				.thenReturn(new BigDecimal("15000.0000"));

		AccountBalanceResponse result = service.getBalance(workspaceId, accountId, ownerId);

		assertEquals(0, new BigDecimal("185000.0000").compareTo(result.currentBalance()));

		assertEquals("ARS", result.currency());
		assertEquals(accountId, result.accountId());
		assertEquals(workspaceId, result.workspaceId());

		verify(workspaceService).findByIdAndOwner(workspaceId, ownerId);
	}

	@Test
	void shouldCalculateBalanceWithOnlyExpense() {

		Account account = mock(Account.class);

		when(accountRepository.findByIdAndWorkspaceId(accountId, workspaceId)).thenReturn(Optional.of(account));

		when(account.getInitialBalance()).thenReturn(new BigDecimal("150000.0000"));

		when(transactionRepository.sumIncomeByAccount(workspaceId, accountId)).thenReturn(BigDecimal.ZERO);

		when(transactionRepository.sumExpenseByAccount(workspaceId, accountId))
				.thenReturn(new BigDecimal("15000.0000"));

		AccountBalanceResponse result = service.getBalance(workspaceId, accountId, ownerId);

		assertEquals(0, new BigDecimal("135000.0000").compareTo(result.currentBalance()));
	}

	@Test
	void shouldReturnInitialBalanceWithoutTransactions() {

		Account account = mock(Account.class);

		when(accountRepository.findByIdAndWorkspaceId(accountId, workspaceId)).thenReturn(Optional.of(account));

		when(account.getInitialBalance()).thenReturn(new BigDecimal("100000.0000"));

		when(transactionRepository.sumIncomeByAccount(workspaceId, accountId)).thenReturn(BigDecimal.ZERO);

		when(transactionRepository.sumExpenseByAccount(workspaceId, accountId)).thenReturn(BigDecimal.ZERO);

		AccountBalanceResponse result = service.getBalance(workspaceId, accountId, ownerId);

		assertEquals(0, new BigDecimal("100000.0000").compareTo(result.currentBalance()));
	}

	@Test
	void shouldRejectForeignWorkspace() {

		when(workspaceService.findByIdAndOwner(workspaceId, ownerId))
				.thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.getBalance(workspaceId, accountId, ownerId));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

		verifyNoInteractions(accountRepository, transactionRepository);
	}

	@Test
	void shouldReturn404ForAccountOutsideWorkspace() {

		when(accountRepository.findByIdAndWorkspaceId(accountId, workspaceId)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.getBalance(workspaceId, accountId, ownerId));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

		verifyNoInteractions(transactionRepository);
	}
}
