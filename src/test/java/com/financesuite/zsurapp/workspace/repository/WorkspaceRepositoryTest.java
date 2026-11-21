
package com.financesuite.zsurapp.workspace.repository;

import static org.junit.jupiter.api.Assertions.*;

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

import com.financesuite.zsurapp.workspace.entity.Workspace;
import com.financesuite.zsurapp.workspace.entity.WorkspaceType;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(
    replace = AutoConfigureTestDatabase.Replace.NONE
)
@TestPropertySource(properties = {
    "SUPABASE_URL=https://auth.test.invalid",
    "DB_PASSWORD=test-only"
})
class WorkspaceRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
        new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private WorkspaceRepository repository;

    @Test
    void shouldPersistWorkspace() {

        UUID ownerId = UUID.randomUUID();

        Workspace workspace = new Workspace(
            "Mi cuenta personal",
            WorkspaceType.PERSONAL,
            ownerId
        );

        Workspace saved =
            repository.saveAndFlush(workspace);

        Optional<Workspace> result =
            repository.findById(saved.getId());

        assertTrue(result.isPresent());

        assertEquals(
            "Mi cuenta personal",
            result.get().getName()
        );

        assertEquals(
            ownerId,
            result.get().getOwnerId()
        );
    }

    @Test
    void shouldListOnlyWorkspacesOfOwner() {

        UUID userA = UUID.randomUUID();
        UUID userB = UUID.randomUUID();

        repository.saveAndFlush(new Workspace(
            "Finanzas de A",
            WorkspaceType.PERSONAL,
            userA
        ));

        repository.saveAndFlush(new Workspace(
            "Finanzas de B",
            WorkspaceType.PERSONAL,
            userB
        ));

        List<Workspace> results =
            repository.findAllByOwnerId(userA);

        assertEquals(1, results.size());

        assertEquals(
            "Finanzas de A",
            results.get(0).getName()
        );

        assertEquals(
            userA,
            results.get(0).getOwnerId()
        );
    }

    @Test
    void shouldNotFindWorkspaceOfAnotherUser() {

        UUID ownerId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();

        Workspace workspace =
            repository.saveAndFlush(new Workspace(
                "Espacio privado",
                WorkspaceType.PERSONAL,
                ownerId
            ));

        Optional<Workspace> result =
            repository.findByIdAndOwnerId(
                workspace.getId(),
                anotherUserId
            );

        assertTrue(result.isEmpty());
    }
}
