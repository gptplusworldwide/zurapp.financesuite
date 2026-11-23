
package com.financesuite.zsurapp.transaction.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import com.financesuite.zsurapp.transaction.dto.CreateTransactionRequest;
import com.financesuite.zsurapp.transaction.dto.TransactionResponse;
import com.financesuite.zsurapp.transaction.entity.Transaction;
import com.financesuite.zsurapp.transaction.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateTransactionRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID ownerId = UUID.fromString(jwt.getSubject());

        Transaction transaction = service.create(
            workspaceId,
            request.accountId(),
            request.categoryId(),
            request.type(),
            request.amount(),
            request.description(),
            request.occurredAt(),
            ownerId
        );

        URI location = URI.create(
            "/api/v1/workspaces/" + workspaceId
            + "/transactions/" + transaction.getId()
        );

        return ResponseEntity.created(location)
            .body(TransactionResponse.from(transaction));
    }

    @GetMapping
    public List<TransactionResponse> findAll(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID ownerId = UUID.fromString(jwt.getSubject());

        return service.findAllByWorkspace(
            workspaceId, ownerId
        ).stream().map(TransactionResponse::from).toList();
    }

    @GetMapping("/{transactionId}")
    public TransactionResponse findById(
            @PathVariable UUID workspaceId,
            @PathVariable UUID transactionId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID ownerId = UUID.fromString(jwt.getSubject());

        Transaction transaction = service.findById(
            workspaceId, transactionId, ownerId
        );

        return TransactionResponse.from(transaction);
    }
}
