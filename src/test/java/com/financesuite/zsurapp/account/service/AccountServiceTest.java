
package com.financesuite.zsurapp.account.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
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
import com.financesuite.zsurapp.account.entity.AccountType;
import com.financesuite.zsurapp.account.repository.AccountRepository;
import com.financesuite.zsurapp.workspace.entity.Workspace;
import com.financesuite.zsurapp.workspace.entity.WorkspaceType;
import com.financesuite.zsurapp.workspace.service.WorkspaceService;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

	@Mock
	private AccountRepository repository;

	@Mock
	private WorkspaceService workspaceService;

	private AccountService service;
	private UUID ownerId;
	private UUID workspaceId;
	private Workspace workspace;

	@BeforeEach
	void setUp() {
		service = new AccountService(repository, workspaceService);

		ownerId = UUID.randomUUID();

		workspace = new Workspace("Finanzas Personales", WorkspaceType.PERSONAL, ownerId);

		workspaceId = workspace.getId();
	}

	@Test
	void shouldCreateAccount() {

		when(workspaceService.findByIdAndOwner(workspaceId, ownerId)).thenReturn(workspace);

		when(repository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

		Account result = service.create(workspaceId, "Efectivo", AccountType.CASH, "ARS", new BigDecimal("150000.00"),
				ownerId);

		assertNotNull(result.getId());
		assertEquals(workspaceId, result.getWorkspaceId());
		assertEquals("Efectivo", result.getName());
		assertEquals(AccountType.CASH, result.getType());
		assertEquals("ARS", result.getCurrency());

		assertEquals(0, new BigDecimal("150000.00").compareTo(result.getInitialBalance()));

		verify(repository).save(any(Account.class));
	}

	@Test
	void shouldRejectCreationInForeignWorkspace() {

		when(workspaceService.findByIdAndOwner(workspaceId, ownerId))
				.thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.create(workspaceId, "Cuenta privada", AccountType.BANK, "ARS", BigDecimal.ZERO, ownerId));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

		verifyNoInteractions(repository);
	}

	@Test
	void shouldListAccountsOfAuthorizedWorkspace() {

		when(workspaceService.findByIdAndOwner(workspaceId, ownerId)).thenReturn(workspace);

		Account account = new Account(workspaceId, "Banco", AccountType.BANK, "ARS", BigDecimal.ZERO);

		when(repository.findAllByWorkspaceId(workspaceId)).thenReturn(List.of(account));

		List<Account> result = service.findAllByWorkspace(workspaceId, ownerId);

		assertEquals(1, result.size());
		assertEquals("Banco", result.get(0).getName());

		verify(repository).findAllByWorkspaceId(workspaceId);
	}

	@Test
	void shouldRejectListingForeignWorkspace() {

		when(workspaceService.findByIdAndOwner(workspaceId, ownerId))
				.thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

		assertThrows(ResponseStatusException.class, () -> service.findAllByWorkspace(workspaceId, ownerId));

		verifyNoInteractions(repository);
	}

	@Test
	void shouldFindAccountInOwnWorkspace() {

		Account account = new Account(workspaceId, "Efectivo", AccountType.CASH, "ARS", BigDecimal.ZERO);

		when(workspaceService.findByIdAndOwner(workspaceId, ownerId)).thenReturn(workspace);

		when(repository.findByIdAndWorkspaceId(account.getId(), workspaceId)).thenReturn(Optional.of(account));

		Account result = service.findById(workspaceId, account.getId(), ownerId);

		assertEquals(account.getId(), result.getId());
	}

	@Test
	void shouldRejectAccountOutsideWorkspace() {

		UUID accountId = UUID.randomUUID();

		when(workspaceService.findByIdAndOwner(workspaceId, ownerId)).thenReturn(workspace);

		when(repository.findByIdAndWorkspaceId(accountId, workspaceId)).thenReturn(Optional.empty());

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.findById(workspaceId, accountId, ownerId));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
	}

	@Test
	void shouldRenameOwnAccount() {

		Account account = new Account(workspaceId, "Efectivo", AccountType.CASH, "ARS", new BigDecimal("150000.00"));

		when(workspaceService.findByIdAndOwner(workspaceId, ownerId)).thenReturn(workspace);

		when(repository.findByIdAndWorkspaceId(account.getId(), workspaceId)).thenReturn(Optional.of(account));

		Account result = service.rename(workspaceId, account.getId(), "Efectivo personal", ownerId);

		assertEquals("Efectivo personal", result.getName());

		assertEquals(0, new BigDecimal("150000.00").compareTo(result.getInitialBalance()));

		assertEquals("ARS", result.getCurrency());

		verify(repository).findByIdAndWorkspaceId(account.getId(), workspaceId);
	}

	@Test
	void shouldRejectRenameInForeignWorkspace() {

		UUID accountId = UUID.randomUUID();

		when(workspaceService.findByIdAndOwner(workspaceId, ownerId))
				.thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

		ResponseStatusException exception = assertThrows(ResponseStatusException.class,
				() -> service.rename(workspaceId, accountId, "Nombre prohibido", ownerId));

		assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());

		verifyNoInteractions(repository);
	}

}
