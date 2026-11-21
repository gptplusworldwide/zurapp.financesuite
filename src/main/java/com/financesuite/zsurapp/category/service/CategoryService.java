
package com.financesuite.zsurapp.category.service;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.financesuite.zsurapp.category.entity.Category;
import com.financesuite.zsurapp.category.entity.CategoryType;
import com.financesuite.zsurapp.category.repository.CategoryRepository;
import com.financesuite.zsurapp.workspace.service.WorkspaceService;

@Service
public class CategoryService {

    private final CategoryRepository repository;
    private final WorkspaceService workspaceService;

    public CategoryService(
            CategoryRepository repository,
            WorkspaceService workspaceService) {

        this.repository = repository;
        this.workspaceService = workspaceService;
    }

    @Transactional
    public Category create(
            UUID workspaceId,
            String name,
            CategoryType type,
            UUID ownerId) {

        workspaceService.findByIdAndOwner(
            workspaceId,
            ownerId
        );

        if (name == null || name.isBlank()
                || name.trim().length() > 120
                || type == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Datos de categoria invalidos"
            );
        }

        String normalizedName = name.trim();

        boolean exists = repository
            .existsByWorkspaceIdAndTypeAndName(
                workspaceId,
                type,
                normalizedName
            );

        if (exists) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La categoria ya existe"
            );
        }

        Category category = new Category(
            workspaceId,
            normalizedName,
            type
        );

        return repository.save(category);
    }

    @Transactional(readOnly = true)
    public List<Category> findAllByWorkspace(
            UUID workspaceId,
            UUID ownerId) {

        workspaceService.findByIdAndOwner(
            workspaceId,
            ownerId
        );

        return repository.findAllByWorkspaceId(
            workspaceId
        );
    }

    @Transactional(readOnly = true)
    public Category findById(
            UUID workspaceId,
            UUID categoryId,
            UUID ownerId) {

        workspaceService.findByIdAndOwner(
            workspaceId,
            ownerId
        );

        return repository.findByIdAndWorkspaceId(
            categoryId,
            workspaceId
        ).orElseThrow(() ->
            new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Categoria no encontrada"
            )
        );
    }
}
