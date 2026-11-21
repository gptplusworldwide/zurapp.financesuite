
package com.financesuite.zsurapp.workspace.service;

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

import com.financesuite.zsurapp.workspace.entity.Workspace;
import com.financesuite.zsurapp.workspace.entity.WorkspaceType;
import com.financesuite.zsurapp.workspace.repository.WorkspaceRepository;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceTest {

    @Mock
    private WorkspaceRepository repository;

    private WorkspaceService service;

    private UUID ownerId;

    @BeforeEach
    void setUp() {
        service = new WorkspaceService(repository);
        ownerId = UUID.randomUUID();
    }

    @Test
    void shouldCreateWorkspace() {
        when(repository.save(any(Workspace.class)))
            .thenAnswer(invocation ->
                invocation.getArgument(0)
            );

        Workspace result = service.create(
            "Mis Finanzas",
            WorkspaceType.PERSONAL,
            ownerId
        );

        assertEquals("Mis Finanzas", result.getName());
        assertEquals(WorkspaceType.PERSONAL, result.getType());
        assertEquals(ownerId, result.getOwnerId());
        assertNotNull(result.getId());

        verify(repository).save(any(Workspace.class));
    }

    @Test
    void shouldRejectBlankName() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.create(
                "   ",
                WorkspaceType.PERSONAL,
                ownerId
            )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectLongName() {
        String longName = "A".repeat(121);

        assertThrows(
            IllegalArgumentException.class,
            () -> service.create(
                longName,
                WorkspaceType.PERSONAL,
                ownerId
            )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldRejectNullType() {
        assertThrows(
            IllegalArgumentException.class,
            () -> service.create(
                "Mi negocio",
                null,
                ownerId
            )
        );

        verifyNoInteractions(repository);
    }

    @Test
    void shouldListOnlyOwnerWorkspaces() {
        Workspace workspace = new Workspace(
            "Mis Finanzas",
            WorkspaceType.PERSONAL,
            ownerId
        );

        when(repository.findAllByOwnerId(ownerId))
            .thenReturn(List.of(workspace));

        List<Workspace> result =
            service.findAllByOwner(ownerId);

        assertEquals(1, result.size());
        assertEquals(ownerId, result.get(0).getOwnerId());

        verify(repository).findAllByOwnerId(ownerId);
    }

    @Test
    void shouldRenameOwnWorkspace() {
        Workspace workspace = new Workspace(
            "Mis Finanzas",
            WorkspaceType.PERSONAL,
            ownerId
        );

        UUID workspaceId = workspace.getId();

        when(repository.findByIdAndOwnerId(
                workspaceId, ownerId))
            .thenReturn(Optional.of(workspace));

        Workspace result = service.rename(
            workspaceId,
            "Finanzas Personales",
            ownerId
        );

        assertEquals(
            "Finanzas Personales",
            result.getName()
        );

        verify(repository).findByIdAndOwnerId(
            workspaceId, ownerId
        );
    }

    @Test
    void shouldRejectWorkspaceNotOwnedByUser() {
        UUID workspaceId = UUID.randomUUID();

        when(repository.findByIdAndOwnerId(
                workspaceId, ownerId))
            .thenReturn(Optional.empty());

        ResponseStatusException exception =
            assertThrows(
                ResponseStatusException.class,
                () -> service.rename(
                    workspaceId,
                    "Nombre modificado",
                    ownerId
                )
            );

        assertEquals(
            HttpStatus.NOT_FOUND,
            exception.getStatusCode()
        );

        verify(repository, never())
            .save(any(Workspace.class));
    }
}
