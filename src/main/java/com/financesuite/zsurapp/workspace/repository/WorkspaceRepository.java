
package com.financesuite.zsurapp.workspace.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financesuite.zsurapp.workspace.entity.Workspace;

public interface WorkspaceRepository
        extends JpaRepository<Workspace, UUID> {

    List<Workspace> findAllByOwnerId(UUID ownerId);

    Optional<Workspace> findByIdAndOwnerId(
            UUID id,
            UUID ownerId
    );
}
