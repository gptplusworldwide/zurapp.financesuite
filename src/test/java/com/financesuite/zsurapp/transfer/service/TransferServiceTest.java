
package com.financesuite.zsurapp.transfer.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.financesuite.zsurapp.account.entity.Account;
import com.financesuite.zsurapp.account.repository.AccountRepository;
import com.financesuite.zsurapp.transfer.entity.Transfer;
import com.financesuite.zsurapp.transfer.repository.TransferRepository;
import com.financesuite.zsurapp.workspace.service.WorkspaceService;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

	@Mock
	private TransferRepository repository;

	@Mock
	private AccountRepository accountRepository;

	@Mock
	private WorkspaceService workspaceService;

	private TransferService service;

	private UUID workspaceId;
	private UUID fromAccountId;
	private UUID toAccountId;
	private UUID ownerId;

	private final Instant occurredAt = Instant.parse("2026-11-23T16:00:00Z");

	@BeforeEach
	void setUp() {

		service = new TransferService(repository, accountRepository, workspaceService);

		workspaceId = UUID.randomUUID();
		fromAccountId = UUID.randomUUID();
		toAccountId = UUID.randomUUID();
		ownerId = UUID.randomUUID();
	}

	@Test
	void shouldCreateTransferBetweenAccounts() {

		Account fromAccount = mock(Account.class);
		Account toAccount = mock(Account.class);

		when(accountRepository.findByIdAndWorkspaceId(fromAccountId, workspaceId)).thenReturn(Optional.of(fromAccount));

		when(accountRepository.findByIdAndWorkspaceId(toAccountId, workspaceId)).thenReturn(Optional.of(toAccount));

		when(fromAccount.getCurrency()).thenReturn("ARS");
		when(toAccount.getCurrency()).thenReturn("ARS");

		when(fromAccount.getId()).thenReturn(fromAccountId);
		when(toAccount.getId()).thenReturn(toAccountId);

		when(repository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Transfer result = service.create(workspaceId, fromAccountId, toAccountId, new BigDecimal("20000.0000"),
				"Transferencia al banco", occurredAt, ownerId);

		assertNotNull(result.getId());
		assertEquals(workspaceId, result.getWorkspaceId());
		assertEquals(fromAccountId, result.getFromAccountId());
		assertEquals(toAccountId, result.getToAccountId());

		assertEquals(0, new BigDecimal("20000.0000").compareTo(result.getAmount()));

		verify(workspaceService).findByIdAndOwner(workspaceId, ownerId);

		verify(repository).save(any(Transfer.class));
	}

	@Test
	void shouldRejectForeignWorkspace() {

		when(workspaceService.findByIdAndOwner(workspaceId, ownerId))
				.thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.create(workspaceId, fromAccountId, toAccountId, new BigDecimal("20000"), "Transferencia",
						occurredAt, ownerId));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

		verifyNoInteractions(accountRepository, repository);
	}

	@Test
	void shouldRejectTransferToSameAccount() {

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.create(workspaceId, fromAccountId, fromAccountId, new BigDecimal("20000"),
						"Transferencia", occurredAt, ownerId));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

		verifyNoInteractions(accountRepository, repository);
	}

	@Test
	void shouldRejectZeroAmount() {

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.create(workspaceId, fromAccountId, toAccountId, BigDecimal.ZERO, "Transferencia",
						occurredAt, ownerId));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

		verifyNoInteractions(accountRepository, repository);
	}

	@Test
	void shouldRejectAccountOutsideWorkspace() {

		when(accountRepository.findByIdAndWorkspaceId(fromAccountId, workspaceId)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.create(workspaceId, fromAccountId, toAccountId, new BigDecimal("20000"), "Transferencia",
						occurredAt, ownerId));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

		verify(repository, never()).save(any());
	}

	@Test
	void shouldRejectDestinationOutsideWorkspace() {

		Account fromAccount = mock(Account.class);

		when(accountRepository.findByIdAndWorkspaceId(fromAccountId, workspaceId)).thenReturn(Optional.of(fromAccount));

		when(accountRepository.findByIdAndWorkspaceId(toAccountId, workspaceId)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.create(workspaceId, fromAccountId, toAccountId, new BigDecimal("20000"), "Transferencia",
						occurredAt, ownerId));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

		verify(repository, never()).save(any());
	}

	@Test
	void shouldRejectDifferentCurrencies() {

		Account fromAccount = mock(Account.class);
		Account toAccount = mock(Account.class);

		when(accountRepository.findByIdAndWorkspaceId(fromAccountId, workspaceId)).thenReturn(Optional.of(fromAccount));

		when(accountRepository.findByIdAndWorkspaceId(toAccountId, workspaceId)).thenReturn(Optional.of(toAccount));

		when(fromAccount.getCurrency()).thenReturn("ARS");
		when(toAccount.getCurrency()).thenReturn("USD");

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.create(workspaceId, fromAccountId, toAccountId, new BigDecimal("20000"), "Transferencia",
						occurredAt, ownerId));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

		verify(repository, never()).save(any());
	}

	@Test
	void shouldRejectMissingDate() {

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.create(workspaceId, fromAccountId, toAccountId, new BigDecimal("20000"), "Transferencia",
						null, ownerId));

		assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

		verifyNoInteractions(accountRepository, repository);
	}

	@Test
	void shouldListWorkspaceTransfers() {

		Transfer transfer = new Transfer(workspaceId, fromAccountId, toAccountId, new BigDecimal("20000.0000"),
				"Transferencia", occurredAt);

		when(repository.findAllByWorkspaceIdOrderByOccurredAtDesc(workspaceId)).thenReturn(List.of(transfer));

		List<Transfer> result = service.findAllByWorkspace(workspaceId, ownerId);

		assertEquals(1, result.size());
		assertEquals(transfer.getId(), result.get(0).getId());

		verify(workspaceService).findByIdAndOwner(workspaceId, ownerId);
	}

	@Test
	void shouldReturn404ForMissingTransfer() {

		UUID transferId = UUID.randomUUID();

		when(repository.findByIdAndWorkspaceId(transferId, workspaceId)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.findById(workspaceId, transferId, ownerId));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}
}
