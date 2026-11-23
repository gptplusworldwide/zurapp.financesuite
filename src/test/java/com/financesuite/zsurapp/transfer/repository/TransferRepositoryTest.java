
package com.financesuite.zsurapp.transfer.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityManager;

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

import com.financesuite.zsurapp.transfer.entity.Transfer;

@SpringBootTest(properties = { "SUPABASE_URL=https://auth.test.invalid", "DB_PASSWORD=test" })
@Testcontainers
@Transactional
class TransferRepositoryTest {

	@Container
	@ServiceConnection
	static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

	@Autowired
	private TransferRepository repository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private EntityManager entityManager;

	private UUID workspaceId;
	private UUID fromAccountId;
	private UUID toAccountId;

	@BeforeEach
	void setUp() {

		workspaceId = UUID.randomUUID();
		fromAccountId = UUID.randomUUID();
		toAccountId = UUID.randomUUID();

		UUID ownerId = UUID.randomUUID();

		jdbcTemplate.update("""
				INSERT INTO workspaces
				    (id, name, type, owner_id)
				VALUES (?, ?, ?, ?)
				""", workspaceId, "Finanzas Personales", "PERSONAL", ownerId);

		jdbcTemplate.update("""
				INSERT INTO accounts
				    (id, workspace_id, name,
				     type, currency, initial_balance)
				VALUES (?, ?, ?, ?, ?, ?)
				""", fromAccountId, workspaceId, "Efectivo personal", "CASH", "ARS", new BigDecimal("150000.0000"));

		jdbcTemplate.update("""
				INSERT INTO accounts
				    (id, workspace_id, name,
				     type, currency, initial_balance)
				VALUES (?, ?, ?, ?, ?, ?)
				""", toAccountId, workspaceId, "Banco personal", "BANK", "ARS", BigDecimal.ZERO);
	}

	private Transfer newTransfer(UUID origin, UUID destination, BigDecimal amount, Instant occurredAt) {

		return new Transfer(workspaceId, origin, destination, amount, "Transferencia entre cuentas", occurredAt);
	}

	@Test
	void shouldSaveTransfer() {

		Transfer transfer = newTransfer(fromAccountId, toAccountId, new BigDecimal("20000.0000"),
				Instant.parse("2026-11-23T16:00:00Z"));

		repository.saveAndFlush(transfer);

		// Forzamos la lectura desde PostgreSQL.
		entityManager.clear();

		Transfer found = repository.findById(transfer.getId()).orElseThrow();

		assertEquals(transfer.getId(), found.getId());
		assertEquals(workspaceId, found.getWorkspaceId());
		assertEquals(fromAccountId, found.getFromAccountId());
		assertEquals(toAccountId, found.getToAccountId());

		assertEquals(0, new BigDecimal("20000.0000").compareTo(found.getAmount()));

		assertNotNull(found.getCreatedAt());
		assertNotNull(found.getUpdatedAt());
	}

	@Test
	void shouldListTransfersOrderedByDate() {

		Transfer older = newTransfer(fromAccountId, toAccountId, new BigDecimal("5000.0000"),
				Instant.parse("2026-11-20T12:00:00Z"));

		Transfer newer = newTransfer(fromAccountId, toAccountId, new BigDecimal("20000.0000"),
				Instant.parse("2026-11-23T16:00:00Z"));

		repository.saveAllAndFlush(List.of(older, newer));

		entityManager.clear();

		List<Transfer> results = repository.findAllByWorkspaceIdOrderByOccurredAtDesc(workspaceId);

		assertEquals(2, results.size());
		assertEquals(newer.getId(), results.get(0).getId());
		assertEquals(older.getId(), results.get(1).getId());
	}

	@Test
	void shouldCalculateIncomingAndOutgoingSeparately() {

		Transfer outgoing = newTransfer(fromAccountId, toAccountId, new BigDecimal("20000.0000"),
				Instant.parse("2026-11-23T16:00:00Z"));

		Transfer incoming = newTransfer(toAccountId, fromAccountId, new BigDecimal("5000.0000"),
				Instant.parse("2026-11-23T17:00:00Z"));

		repository.saveAllAndFlush(List.of(outgoing, incoming));

		BigDecimal cashIncoming = repository.sumIncomingByAccount(workspaceId, fromAccountId);

		BigDecimal cashOutgoing = repository.sumOutgoingByAccount(workspaceId, fromAccountId);

		BigDecimal bankIncoming = repository.sumIncomingByAccount(workspaceId, toAccountId);

		BigDecimal bankOutgoing = repository.sumOutgoingByAccount(workspaceId, toAccountId);

		assertEquals(0, new BigDecimal("5000.0000").compareTo(cashIncoming));

		assertEquals(0, new BigDecimal("20000.0000").compareTo(cashOutgoing));

		assertEquals(0, new BigDecimal("20000.0000").compareTo(bankIncoming));

		assertEquals(0, new BigDecimal("5000.0000").compareTo(bankOutgoing));
	}

	@Test
	void shouldReturnZeroWithoutTransfers() {

		BigDecimal incoming = repository.sumIncomingByAccount(workspaceId, fromAccountId);

		BigDecimal outgoing = repository.sumOutgoingByAccount(workspaceId, fromAccountId);

		assertNotNull(incoming);
		assertNotNull(outgoing);

		assertEquals(0, BigDecimal.ZERO.compareTo(incoming));

		assertEquals(0, BigDecimal.ZERO.compareTo(outgoing));
	}

	@Test
	void shouldNotExposeTransfersAcrossWorkspaces() {

		Transfer transfer = newTransfer(fromAccountId, toAccountId, new BigDecimal("20000.0000"),
				Instant.parse("2026-11-23T16:00:00Z"));

		repository.saveAndFlush(transfer);

		UUID foreignWorkspaceId = UUID.randomUUID();

		assertTrue(repository.findByIdAndWorkspaceId(transfer.getId(), foreignWorkspaceId).isEmpty());

		assertTrue(repository.findAllByWorkspaceIdOrderByOccurredAtDesc(foreignWorkspaceId).isEmpty());

		BigDecimal incoming = repository.sumIncomingByAccount(foreignWorkspaceId, toAccountId);

		BigDecimal outgoing = repository.sumOutgoingByAccount(foreignWorkspaceId, fromAccountId);

		assertEquals(0, BigDecimal.ZERO.compareTo(incoming));

		assertEquals(0, BigDecimal.ZERO.compareTo(outgoing));
	}
}
