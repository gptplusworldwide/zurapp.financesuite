
package com.financesuite.zsurapp.transaction.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.financesuite.zsurapp.transaction.entity.Transaction;
import com.financesuite.zsurapp.transaction.entity.TransactionType;

@SpringBootTest(properties = { "SUPABASE_URL=https://auth.test.invalid", "DB_PASSWORD=test" })
@Testcontainers
@Transactional
class TransactionRepositoryTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

	@Autowired
	private TransactionRepository repository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private UUID workspaceId;
	private UUID accountId;
	private UUID categoryId;

	@BeforeEach
	void setUp() {

		workspaceId = UUID.randomUUID();
		accountId = UUID.randomUUID();
		categoryId = UUID.randomUUID();

		UUID ownerId = UUID.randomUUID();

		jdbcTemplate.update("""
				INSERT INTO workspaces
				    (id, name, type, owner_id)
				VALUES (?, ?, ?, ?)
				""", workspaceId, "Finanzas Personales", "PERSONAL", ownerId);

		jdbcTemplate.update("""
				INSERT INTO accounts
				    (id, workspace_id, name, type,
				     currency, initial_balance)
				VALUES (?, ?, ?, ?, ?, ?)
				""", accountId, workspaceId, "Efectivo personal", "CASH", "ARS", new BigDecimal("150000.0000"));

		jdbcTemplate.update("""
				INSERT INTO categories
				    (id, workspace_id, name, type)
				VALUES (?, ?, ?, ?)
				""", categoryId, workspaceId, "Alimentación", "EXPENSE");
	}

	private Transaction newTransaction(BigDecimal amount, Instant occurredAt) {

		return new Transaction(workspaceId, accountId, categoryId, TransactionType.EXPENSE, amount,
				"Compra del supermercado", occurredAt);
	}

	@Test
	void shouldSaveTransaction() {

		Transaction transaction = newTransaction(new BigDecimal("15000.0000"), Instant.parse("2026-11-23T12:00:00Z"));

		repository.saveAndFlush(transaction);

		Transaction found = repository.findById(transaction.getId()).orElseThrow();

		assertEquals(transaction.getId(), found.getId());
		assertEquals(workspaceId, found.getWorkspaceId());
		assertEquals(accountId, found.getAccountId());
		assertEquals(categoryId, found.getCategoryId());

		assertEquals(0, new BigDecimal("15000.0000").compareTo(found.getAmount()));

		assertNotNull(found.getCreatedAt());
		assertNotNull(found.getUpdatedAt());
	}

	@Test
	void shouldListTransactionsOrderedByDate() {

		Transaction older = newTransaction(new BigDecimal("1000.0000"), Instant.parse("2026-11-20T12:00:00Z"));

		Transaction newer = newTransaction(new BigDecimal("2000.0000"), Instant.parse("2026-11-23T12:00:00Z"));

		repository.saveAndFlush(older);
		repository.saveAndFlush(newer);

		List<Transaction> results = repository.findAllByWorkspaceIdOrderByOccurredAtDesc(workspaceId);

		assertEquals(2, results.size());
		assertEquals(newer.getId(), results.get(0).getId());
		assertEquals(older.getId(), results.get(1).getId());
	}

	@Test
	void shouldFilterTransactionsByAccount() {

		Transaction transaction = newTransaction(new BigDecimal("5000.0000"), Instant.parse("2026-11-23T12:00:00Z"));

		repository.saveAndFlush(transaction);

		List<Transaction> matching = repository.findAllByWorkspaceIdAndAccountIdOrderByOccurredAtDesc(workspaceId,
				accountId);

		List<Transaction> notMatching = repository.findAllByWorkspaceIdAndAccountIdOrderByOccurredAtDesc(workspaceId,
				UUID.randomUUID());

		assertEquals(1, matching.size());
		assertEquals(transaction.getId(), matching.get(0).getId());
		assertTrue(notMatching.isEmpty());
	}

	@Test
	void shouldNotFindTransactionInAnotherWorkspace() {

		Transaction transaction = newTransaction(new BigDecimal("15000.0000"), Instant.parse("2026-11-23T12:00:00Z"));

		repository.saveAndFlush(transaction);

		UUID foreignWorkspaceId = UUID.randomUUID();

		assertTrue(repository.findByIdAndWorkspaceId(transaction.getId(), foreignWorkspaceId).isEmpty());

		assertTrue(repository.findByIdAndWorkspaceId(transaction.getId(), workspaceId).isPresent());
	}
}
