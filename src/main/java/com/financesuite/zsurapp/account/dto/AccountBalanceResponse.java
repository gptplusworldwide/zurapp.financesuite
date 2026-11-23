
package com.financesuite.zsurapp.account.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountBalanceResponse(UUID accountId, UUID workspaceId, String name, String currency,
		BigDecimal initialBalance, BigDecimal totalIncome, BigDecimal totalExpense, BigDecimal incomingTransfers,
		BigDecimal outgoingTransfers, BigDecimal currentBalance) {
}
