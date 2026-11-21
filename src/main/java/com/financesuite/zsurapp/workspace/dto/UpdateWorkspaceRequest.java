
package com.financesuite.zsurapp.workspace.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateWorkspaceRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "Maximo 120 caracteres")
    String name

) {}
