
package com.financesuite.zsurapp.category.dto;

import java.time.Instant;
import java.util.UUID;

import com.financesuite.zsurapp.category.entity.Category;
import com.financesuite.zsurapp.category.entity.CategoryType;

public record CategoryResponse(

    UUID id,
    UUID workspaceId,
    String name,
    CategoryType type,
    Instant createdAt,
    Instant updatedAt

) {

    public static CategoryResponse from(Category category) {

        return new CategoryResponse(
            category.getId(),
            category.getWorkspaceId(),
            category.getName(),
            category.getType(),
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}
