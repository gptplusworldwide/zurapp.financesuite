
package com.financesuite.zsurapp.category.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.financesuite.zsurapp.category.dto.CategoryResponse;
import com.financesuite.zsurapp.category.dto.CreateCategoryRequest;
import com.financesuite.zsurapp.category.entity.Category;
import com.financesuite.zsurapp.category.service.CategoryService;

@RestController
@RequestMapping(
    "/api/v1/workspaces/{workspaceId}/categories"
)
public class CategoryController {

    private final CategoryService service;

    public CategoryController(CategoryService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<CategoryResponse> create(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateCategoryRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID ownerId = UUID.fromString(
            jwt.getSubject()
        );

        Category category = service.create(
            workspaceId,
            request.name(),
            request.type(),
            ownerId
        );

        URI location = URI.create(
            "/api/v1/workspaces/" + workspaceId
            + "/categories/" + category.getId()
        );

        return ResponseEntity
            .created(location)
            .body(CategoryResponse.from(category));
    }

    @GetMapping
    public List<CategoryResponse> findAll(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID ownerId = UUID.fromString(
            jwt.getSubject()
        );

        return service.findAllByWorkspace(
                workspaceId, ownerId
            )
            .stream()
            .map(CategoryResponse::from)
            .toList();
    }

    @GetMapping("/{categoryId}")
    public CategoryResponse findById(
            @PathVariable UUID workspaceId,
            @PathVariable UUID categoryId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID ownerId = UUID.fromString(
            jwt.getSubject()
        );

        Category category = service.findById(
            workspaceId,
            categoryId,
            ownerId
        );

        return CategoryResponse.from(category);
    }
}
