
package com.financesuite.zsurapp.account.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateAccountRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120, message = "Maximo 120 caracteres")
    String name

) {}
