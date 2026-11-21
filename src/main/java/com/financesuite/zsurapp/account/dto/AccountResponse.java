
package com.financesuite.zsurapp.account.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.financesuite.zsurapp.account.entity.Account;
import com.financesuite.zsurapp.account.entity.AccountType;

public record AccountResponse(

    UUID id,
    UUID workspaceId,
    String name,
    AccountType type,
    String currency,
    BigDecimal initialBalance,
    Instant createdAt,
    Instant updatedAt

) {

    public static AccountResponse from(Account account) {

        return new AccountResponse(
            account.getId(),
            account.getWorkspaceId(),
            account.getName(),
            account.getType(),
            account.getCurrency(),
            account.getInitialBalance(),
            account.getCreatedAt(),
            account.getUpdatedAt()
        );
    }
}
