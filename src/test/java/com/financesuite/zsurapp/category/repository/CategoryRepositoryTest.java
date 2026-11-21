
package com.financesuite.zsurapp.category.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import com.financesuite.zsurapp.category.entity.Category;
import com.financesuite.zsurapp.category.entity.CategoryType;
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
class CategoryRepositoryTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
        new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private CategoryRepository categoryRepository;

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
    void shouldPersistCategory() {

        Workspace workspace =
            createWorkspace("Finanzas Personales");

        Category category = new Category(
            workspace.getId(),
            "Alimentación",
            CategoryType.EXPENSE
        );

        Category saved =
            categoryRepository.saveAndFlush(category);

        Optional<Category> result =
            categoryRepository.findById(saved.getId());

        assertTrue(result.isPresent());

        Category found = result.orElseThrow();

        assertEquals("Alimentación", found.getName());
        assertEquals(CategoryType.EXPENSE, found.getType());
        assertEquals(
            workspace.getId(),
            found.getWorkspaceId()
        );

        assertNotNull(found.getCreatedAt());
        assertNotNull(found.getUpdatedAt());
    }

    @Test
    void shouldListCategoriesOnlyOfWorkspace() {

        Workspace workspaceA =
            createWorkspace("Finanzas A");

        Workspace workspaceB =
            createWorkspace("Finanzas B");

        categoryRepository.saveAndFlush(new Category(
            workspaceA.getId(),
            "Alimentación",
            CategoryType.EXPENSE
        ));

        categoryRepository.saveAndFlush(new Category(
            workspaceB.getId(),
            "Sueldo",
            CategoryType.INCOME
        ));

        List<Category> result =
            categoryRepository.findAllByWorkspaceId(
                workspaceA.getId()
            );

        assertEquals(1, result.size());
        assertEquals(
            "Alimentación",
            result.get(0).getName()
        );
        assertEquals(
            workspaceA.getId(),
            result.get(0).getWorkspaceId()
        );
    }

    @Test
    void shouldNotFindCategoryInDifferentWorkspace() {

        Workspace workspaceA =
            createWorkspace("Finanzas A");

        Workspace workspaceB =
            createWorkspace("Finanzas B");

        Category saved =
            categoryRepository.saveAndFlush(new Category(
                workspaceA.getId(),
                "Sueldo",
                CategoryType.INCOME
            ));

        Optional<Category> result =
            categoryRepository.findByIdAndWorkspaceId(
                saved.getId(),
                workspaceB.getId()
            );

        assertTrue(result.isEmpty());
    }

    @Test
    void shouldRejectDuplicateCategoryInDatabase() {

        Workspace workspace =
            createWorkspace("Finanzas Personales");

        categoryRepository.saveAndFlush(new Category(
            workspace.getId(),
            "Alimentación",
            CategoryType.EXPENSE
        ));

        assertThrows(
            DataIntegrityViolationException.class,
            () -> categoryRepository.saveAndFlush(
                new Category(
                    workspace.getId(),
                    "Alimentación",
                    CategoryType.EXPENSE
                )
            )
        );
    }
}
