
package com.financesuite.zsurapp.transaction.service;

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
import com.financesuite.zsurapp.category.entity.Category;
import com.financesuite.zsurapp.category.entity.CategoryType;
import com.financesuite.zsurapp.category.repository.CategoryRepository;
import com.financesuite.zsurapp.transaction.entity.Transaction;
import com.financesuite.zsurapp.transaction.entity.TransactionType;
import com.financesuite.zsurapp.transaction.repository.TransactionRepository;
import com.financesuite.zsurapp.workspace.service.WorkspaceService;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private TransactionRepository repository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private WorkspaceService workspaceService;

    private TransactionService service;

    private UUID workspaceId;
    private UUID accountId;
    private UUID categoryId;
    private UUID ownerId;

    private final Instant occurredAt =
        Instant.parse("2026-11-23T12:00:00Z");

    @BeforeEach
    void setUp() {
        service = new TransactionService(
            repository,
            accountRepository,
            categoryRepository,
            workspaceService
        );

        workspaceId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        categoryId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
    }

    @Test
    void shouldCreateExpenseTransaction() {

        Account account = mock(Account.class);
        Category category = mock(Category.class);

        when(accountRepository.findByIdAndWorkspaceId(
                accountId, workspaceId))
            .thenReturn(Optional.of(account));

        when(categoryRepository.findByIdAndWorkspaceId(
                categoryId, workspaceId))
            .thenReturn(Optional.of(category));

        when(account.getId()).thenReturn(accountId);
        when(category.getId()).thenReturn(categoryId);
        when(category.getType())
            .thenReturn(CategoryType.EXPENSE);

        when(repository.save(any(Transaction.class)))
            .thenAnswer(invocation ->
                invocation.getArgument(0));

        Transaction result = service.create(
            workspaceId,
            accountId,
            categoryId,
            TransactionType.EXPENSE,
            new BigDecimal("15000.0000"),
            "Compra del supermercado",
            occurredAt,
            ownerId
        );

        assertNotNull(result.getId());
        assertEquals(workspaceId, result.getWorkspaceId());
        assertEquals(accountId, result.getAccountId());
        assertEquals(categoryId, result.getCategoryId());
        assertEquals(TransactionType.EXPENSE, result.getType());
        assertEquals(
            0,
            new BigDecimal("15000.0000")
                .compareTo(result.getAmount())
        );

        verify(repository).save(any(Transaction.class));
        verify(workspaceService)
            .findByIdAndOwner(workspaceId, ownerId);
    }

    @Test
    void shouldRejectForeignWorkspace() {

        when(workspaceService.findByIdAndOwner(
                workspaceId, ownerId))
            .thenThrow(new ResponseStatusException(
                HttpStatus.NOT_FOUND
            ));

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.create(
                workspaceId,
                accountId,
                categoryId,
                TransactionType.EXPENSE,
                new BigDecimal("100"),
                "Compra",
                occurredAt,
                ownerId
            )
        );

        assertEquals(
            HttpStatus.NOT_FOUND,
            exception.getStatusCode()
        );

        verifyNoInteractions(
            accountRepository,
            categoryRepository,
            repository
        );
    }

    @Test
    void shouldRejectAccountOutsideWorkspace() {

        when(accountRepository.findByIdAndWorkspaceId(
                accountId, workspaceId))
            .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.create(
                workspaceId,
                accountId,
                categoryId,
                TransactionType.EXPENSE,
                new BigDecimal("100"),
                "Compra",
                occurredAt,
                ownerId
            )
        );

        assertEquals(
            HttpStatus.NOT_FOUND,
            exception.getStatusCode()
        );

        verifyNoInteractions(categoryRepository, repository);
    }

    @Test
    void shouldRejectCategoryOutsideWorkspace() {

        Account account = mock(Account.class);

        when(accountRepository.findByIdAndWorkspaceId(
                accountId, workspaceId))
            .thenReturn(Optional.of(account));

        when(categoryRepository.findByIdAndWorkspaceId(
                categoryId, workspaceId))
            .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.create(
                workspaceId,
                accountId,
                categoryId,
                TransactionType.EXPENSE,
                new BigDecimal("100"),
                "Compra",
                occurredAt,
                ownerId
            )
        );

        assertEquals(
            HttpStatus.NOT_FOUND,
            exception.getStatusCode()
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectCategoryTypeMismatch() {

        Account account = mock(Account.class);
        Category category = mock(Category.class);

        when(accountRepository.findByIdAndWorkspaceId(
                accountId, workspaceId))
            .thenReturn(Optional.of(account));

        when(categoryRepository.findByIdAndWorkspaceId(
                categoryId, workspaceId))
            .thenReturn(Optional.of(category));

        when(category.getType())
            .thenReturn(CategoryType.INCOME);

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.create(
                workspaceId,
                accountId,
                categoryId,
                TransactionType.EXPENSE,
                new BigDecimal("100"),
                "Compra",
                occurredAt,
                ownerId
            )
        );

        assertEquals(
            HttpStatus.BAD_REQUEST,
            exception.getStatusCode()
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldListWorkspaceTransactions() {

        Transaction transaction = new Transaction(
            workspaceId,
            accountId,
            categoryId,
            TransactionType.EXPENSE,
            new BigDecimal("15000.0000"),
            "Compra",
            occurredAt
        );

        when(repository
                .findAllByWorkspaceIdOrderByOccurredAtDesc(
                    workspaceId))
            .thenReturn(List.of(transaction));

        List<Transaction> result =
            service.findAllByWorkspace(workspaceId, ownerId);

        assertEquals(1, result.size());
        assertEquals(transaction.getId(), result.get(0).getId());

        verify(workspaceService)
            .findByIdAndOwner(workspaceId, ownerId);
    }

    @Test
    void shouldReturn404ForMissingTransaction() {

        UUID transactionId = UUID.randomUUID();

        when(repository.findByIdAndWorkspaceId(
                transactionId, workspaceId))
            .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
            ResponseStatusException.class,
            () -> service.findById(
                workspaceId,
                transactionId,
                ownerId
            )
        );

        assertEquals(
            HttpStatus.NOT_FOUND,
            exception.getStatusCode()
        );
    }
}
