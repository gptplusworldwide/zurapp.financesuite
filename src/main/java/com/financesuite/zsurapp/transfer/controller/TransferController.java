
package com.financesuite.zsurapp.transfer.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financesuite.zsurapp.transfer.dto.CreateTransferRequest;
import com.financesuite.zsurapp.transfer.dto.TransferResponse;
import com.financesuite.zsurapp.transfer.entity.Transfer;
import com.financesuite.zsurapp.transfer.service.TransferService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/workspaces/{workspaceId}/transfers")
public class TransferController {

    private final TransferService service;

    public TransferController(TransferService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> create(
            @PathVariable UUID workspaceId,
            @Valid @RequestBody CreateTransferRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        UUID ownerId = UUID.fromString(jwt.getSubject());

        Transfer transfer = service.create(
            workspaceId,
            request.fromAccountId(),
            request.toAccountId(),
            request.amount(),
            request.description(),
            request.occurredAt(),
            ownerId
        );

        URI location = URI.create(
            "/api/v1/workspaces/" + workspaceId
                + "/transfers/" + transfer.getId()
        );

        return ResponseEntity.created(location)
            .body(TransferResponse.from(transfer));
    }

    @GetMapping
    public List<TransferResponse> findAll(
            @PathVariable UUID workspaceId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID ownerId = UUID.fromString(jwt.getSubject());

        return service.findAllByWorkspace(
            workspaceId,
            ownerId
        ).stream()
            .map(TransferResponse::from)
            .toList();
    }

    @GetMapping("/{transferId}")
    public TransferResponse findById(
            @PathVariable UUID workspaceId,
            @PathVariable UUID transferId,
            @AuthenticationPrincipal Jwt jwt) {

        UUID ownerId = UUID.fromString(jwt.getSubject());

        Transfer transfer = service.findById(
            workspaceId,
            transferId,
            ownerId
        );

        return TransferResponse.from(transfer);
    }
}
