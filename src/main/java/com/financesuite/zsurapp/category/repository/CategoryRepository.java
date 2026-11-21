
package com.financesuite.zsurapp.category.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financesuite.zsurapp.category.entity.Category;
import com.financesuite.zsurapp.category.entity.CategoryType;

public interface CategoryRepository
        extends JpaRepository<Category, UUID> {

    List<Category> findAllByWorkspaceId(
        UUID workspaceId
    );

    Optional<Category> findByIdAndWorkspaceId(
        UUID id,
        UUID workspaceId
    );

    boolean existsByWorkspaceIdAndTypeAndName(
        UUID workspaceId,
        CategoryType type,
        String name
    );
}
