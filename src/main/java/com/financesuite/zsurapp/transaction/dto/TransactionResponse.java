
package com.financesuite.zsurapp.transaction.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.financesuite.zsurapp.transaction.entity.Transaction;
import com.financesuite.zsurapp.transaction.entity.TransactionType;

public record TransactionResponse(
    UUID id,
    UUID workspaceId,
    UUID accountId,
    UUID categoryId,
    TransactionType type,
    BigDecimal amount,
    String description,
    Instant occurredAt,
    Instant createdAt,
    Instant updatedAt
) {

    public static TransactionResponse from(
            Transaction transaction) {

        return new TransactionResponse(
            transaction.getId(),
            transaction.getWorkspaceId(),
            transaction.getAccountId(),
            transaction.getCategoryId(),
            transaction.getType(),
            transaction.getAmount(),
            transaction.getDescription(),
            transaction.getOccurredAt(),
            transaction.getCreatedAt(),
            transaction.getUpdatedAt()
        );
    }
}
