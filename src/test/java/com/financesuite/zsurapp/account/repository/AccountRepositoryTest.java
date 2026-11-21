
package com.financesuite.zsurapp.account.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.financesuite.zsurapp.account.entity.Account;
import com.financesuite.zsurapp.account.entity.AccountType;
import com.financesuite.zsurapp.workspace.entity.Workspace;
import com.financesuite.zsurapp.workspace.entity.WorkspaceType;
import com.financesuite.zsurapp.workspace.repository.WorkspaceRepository;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
@TestPropertySource(properties = {
    "SUPABASE_URL=https://auth.test.invalid",
    "DB_PASSWORD=test-only"
})
class AccountRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
        new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private WorkspaceRepository workspaceRepository;

    private Workspace createWorkspace(String name) {

        Workspace workspace = new Workspace(
            name,
            WorkspaceType.PERSONAL,
            UUID.randomUUID()
        );

        return workspaceRepository.saveAndFlush(workspace);
    }

    @Test
    void shouldPersistAccount() {

        Workspace workspace =
            createWorkspace("Finanzas Personales");

        Account account = new Account(
            workspace.getId(),
            "Efectivo",
            AccountType.CASH,
            "ARS",
            new BigDecimal("150000.00")
        );

        Account saved =
            accountRepository.saveAndFlush(account);

        Optional<Account> result =
            accountRepository.findById(saved.getId());

        assertTrue(result.isPresent());

        Account found = result.orElseThrow();

        assertEquals("Efectivo", found.getName());
        assertEquals(AccountType.CASH, found.getType());
        assertEquals("ARS", found.getCurrency());

        assertEquals(
            workspace.getId(),
            found.getWorkspaceId()
        );

        assertEquals(
            0,
            new BigDecimal("150000.00")
                .compareTo(found.getInitialBalance())
        );
    }

    @Test
    void shouldListAccountsOnlyOfWorkspace() {

        Workspace workspaceA =
            createWorkspace("Finanzas A");

        Workspace workspaceB =
            createWorkspace("Finanzas B");

        accountRepository.saveAndFlush(new Account(
            workspaceA.getId(),
            "Efectivo A",
            AccountType.CASH,
            "ARS",
            BigDecimal.ZERO
        ));

        accountRepository.saveAndFlush(new Account(
            workspaceB.getId(),
            "Banco B",
            AccountType.BANK,
            "USD",
            BigDecimal.ZERO
        ));

        List<Account> results =
            accountRepository.findAllByWorkspaceId(
                workspaceA.getId()
            );

        assertEquals(1, results.size());
        assertEquals("Efectivo A", results.get(0).getName());

        assertEquals(
            workspaceA.getId(),
            results.get(0).getWorkspaceId()
        );
    }

    @Test
    void shouldNotFindAccountInDifferentWorkspace() {

        Workspace workspaceA =
            createWorkspace("Finanzas A");

        Workspace workspaceB =
            createWorkspace("Finanzas B");

        Account account =
            accountRepository.saveAndFlush(new Account(
                workspaceA.getId(),
                "Cuenta privada",
                AccountType.BANK,
                "ARS",
                BigDecimal.ZERO
            ));

        Optional<Account> result =
            accountRepository.findByIdAndWorkspaceId(
                account.getId(),
                workspaceB.getId()
            );

        assertTrue(result.isEmpty());
    }
}
