
package com.financesuite.zsurapp.category.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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

import com.financesuite.zsurapp.category.entity.Category;
import com.financesuite.zsurapp.category.entity.CategoryType;
import com.financesuite.zsurapp.category.repository.CategoryRepository;
import com.financesuite.zsurapp.workspace.entity.Workspace;
import com.financesuite.zsurapp.workspace.entity.WorkspaceType;
import com.financesuite.zsurapp.workspace.service.WorkspaceService;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository repository;

    @Mock
    private WorkspaceService workspaceService;

    private CategoryService service;
    private UUID ownerId;
    private UUID workspaceId;
    private Workspace workspace;

    @BeforeEach
    void setUp() {

        service = new CategoryService(
            repository,
            workspaceService
        );

        ownerId = UUID.randomUUID();

        workspace = new Workspace(
            "Finanzas Personales",
            WorkspaceType.PERSONAL,
            ownerId
        );

        workspaceId = workspace.getId();
    }

    @Test
    void shouldCreateExpenseCategory() {

        when(workspaceService.findByIdAndOwner(
                workspaceId, ownerId))
            .thenReturn(workspace);

        when(repository.save(any(Category.class)))
            .thenAnswer(invocation ->
                invocation.getArgument(0)
            );

        Category result = service.create(
            workspaceId,
            "Alimentación",
            CategoryType.EXPENSE,
            ownerId
        );

        assertNotNull(result.getId());
        assertEquals(workspaceId, result.getWorkspaceId());
        assertEquals("Alimentación", result.getName());
        assertEquals(CategoryType.EXPENSE, result.getType());

        verify(repository).save(any(Category.class));
    }

    @Test
    void shouldCreateIncomeCategory() {

        when(workspaceService.findByIdAndOwner(
                workspaceId, ownerId))
            .thenReturn(workspace);

        when(repository.save(any(Category.class)))
            .thenAnswer(invocation ->
                invocation.getArgument(0)
            );

        Category result = service.create(
            workspaceId,
            "Sueldo",
            CategoryType.INCOME,
            ownerId
        );

        assertEquals("Sueldo", result.getName());
        assertEquals(CategoryType.INCOME, result.getType());
    }

    @Test
    void shouldRejectDuplicateCategory() {

        when(workspaceService.findByIdAndOwner(
                workspaceId, ownerId))
            .thenReturn(workspace);

        when(repository.existsByWorkspaceIdAndTypeAndName(
                workspaceId,
                CategoryType.EXPENSE,
                "Alimentación"
            ))
            .thenReturn(true);

        ResponseStatusException exception =
            assertThrows(
                ResponseStatusException.class,
                () -> service.create(
                    workspaceId,
                    "Alimentación",
                    CategoryType.EXPENSE,
                    ownerId
                )
            );

        assertEquals(
            HttpStatus.CONFLICT,
            exception.getStatusCode()
        );

        verify(repository, never()).save(any());
    }

    @Test
    void shouldRejectCreationInForeignWorkspace() {

        when(workspaceService.findByIdAndOwner(
                workspaceId, ownerId))
            .thenThrow(new ResponseStatusException(
                HttpStatus.NOT_FOUND
            ));

        assertThrows(
            ResponseStatusException.class,
            () -> service.create(
                workspaceId,
                "Privada",
                CategoryType.EXPENSE,
                ownerId
            )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldListCategoriesOfOwnWorkspace() {

        when(workspaceService.findByIdAndOwner(
                workspaceId, ownerId))
            .thenReturn(workspace);

        Category category = new Category(
            workspaceId,
            "Transporte",
            CategoryType.EXPENSE
        );

        when(repository.findAllByWorkspaceId(workspaceId))
            .thenReturn(List.of(category));

        List<Category> result =
            service.findAllByWorkspace(
                workspaceId,
                ownerId
            );

        assertEquals(1, result.size());
        assertEquals("Transporte", result.get(0).getName());

        verify(repository)
            .findAllByWorkspaceId(workspaceId);
    }

    @Test
    void shouldRejectListingForeignWorkspace() {

        when(workspaceService.findByIdAndOwner(
                workspaceId, ownerId))
            .thenThrow(new ResponseStatusException(
                HttpStatus.NOT_FOUND
            ));

        assertThrows(
            ResponseStatusException.class,
            () -> service.findAllByWorkspace(
                workspaceId,
                ownerId
            )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldReturn404ForMissingCategory() {

        UUID categoryId = UUID.randomUUID();

        when(workspaceService.findByIdAndOwner(
                workspaceId, ownerId))
            .thenReturn(workspace);

        when(repository.findByIdAndWorkspaceId(
                categoryId,
                workspaceId
            ))
            .thenReturn(Optional.empty());

        ResponseStatusException exception =
            assertThrows(
                ResponseStatusException.class,
                () -> service.findById(
                    workspaceId,
                    categoryId,
                    ownerId
                )
            );

        assertEquals(
            HttpStatus.NOT_FOUND,
            exception.getStatusCode()
        );
    }
}
