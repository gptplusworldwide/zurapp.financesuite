
package com.financesuite.zsurapp.workspace.dto;

import java.time.Instant;
import java.util.UUID;

import com.financesuite.zsurapp.workspace.entity.Workspace;
import com.financesuite.zsurapp.workspace.entity.WorkspaceType;

public record WorkspaceResponse(
    UUID id,
    String name,
    WorkspaceType type,
    Instant createdAt,
    Instant updatedAt
) {

    public static WorkspaceResponse from(Workspace workspace) {
        return new WorkspaceResponse(
            workspace.getId(),
            workspace.getName(),
            workspace.getType(),
            workspace.getCreatedAt(),
            workspace.getUpdatedAt()
        );
    }
}
