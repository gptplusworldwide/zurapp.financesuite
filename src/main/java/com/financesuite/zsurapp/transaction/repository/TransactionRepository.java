
package com.financesuite.zsurapp.transaction.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;

import com.financesuite.zsurapp.transaction.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

	List<Transaction> findAllByWorkspaceIdOrderByOccurredAtDesc(UUID workspaceId);

	List<Transaction> findAllByWorkspaceIdAndAccountIdOrderByOccurredAtDesc(UUID workspaceId, UUID accountId);

	Optional<Transaction> findByIdAndWorkspaceId(UUID transactionId, UUID workspaceId);

	@Query("""
			SELECT COALESCE(SUM(t.amount), 0)
			FROM Transaction t
			WHERE t.workspaceId = :workspaceId
			  AND t.accountId = :accountId
			  AND t.type = 'INCOME'
			""")
	BigDecimal sumIncomeByAccount(@Param("workspaceId") UUID workspaceId, @Param("accountId") UUID accountId);

	@Query("""
			SELECT COALESCE(SUM(t.amount), 0)
			FROM Transaction t
			WHERE t.workspaceId = :workspaceId
			  AND t.accountId = :accountId
			  AND t.type = 'EXPENSE'
			""")
	BigDecimal sumExpenseByAccount(@Param("workspaceId") UUID workspaceId, @Param("accountId") UUID accountId);

}
