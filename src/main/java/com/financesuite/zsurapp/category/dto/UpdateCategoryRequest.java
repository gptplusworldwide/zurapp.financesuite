
package com.financesuite.zsurapp.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateCategoryRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120)
    String name

) {}
