
package com.financesuite.zsurapp.workspace.dto;

import com.financesuite.zsurapp.workspace.entity.WorkspaceType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateWorkspaceRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "Maximo 120 caracteres")
    String name,

    @NotNull(message = "El tipo es obligatorio")
    WorkspaceType type

) {}
