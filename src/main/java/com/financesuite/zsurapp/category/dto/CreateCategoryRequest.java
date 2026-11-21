
package com.financesuite.zsurapp.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.financesuite.zsurapp.category.entity.CategoryType;

public record CreateCategoryRequest(

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 120)
    String name,

    @NotNull(message = "El tipo es obligatorio")
    CategoryType type

) {}
