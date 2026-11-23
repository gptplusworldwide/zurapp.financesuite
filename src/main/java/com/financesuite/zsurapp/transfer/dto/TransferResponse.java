
package com.financesuite.zsurapp.transfer.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.financesuite.zsurapp.transfer.entity.Transfer;

public record TransferResponse(
    UUID id,
    UUID workspaceId,
    UUID fromAccountId,
    UUID toAccountId,
    BigDecimal amount,
    String description,
    Instant occurredAt,
    Instant createdAt,
    Instant updatedAt
) {

    public static TransferResponse from(Transfer transfer) {

        return new TransferResponse(
            transfer.getId(),
            transfer.getWorkspaceId(),
            transfer.getFromAccountId(),
            transfer.getToAccountId(),
            transfer.getAmount(),
            transfer.getDescription(),
            transfer.getOccurredAt(),
            transfer.getCreatedAt(),
            transfer.getUpdatedAt()
        );
    }
}
