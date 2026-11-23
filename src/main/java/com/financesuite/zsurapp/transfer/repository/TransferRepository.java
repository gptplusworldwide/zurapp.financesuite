
package com.financesuite.zsurapp.transfer.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.financesuite.zsurapp.transfer.entity.Transfer;

public interface TransferRepository extends JpaRepository<Transfer, UUID> {

	List<Transfer> findAllByWorkspaceIdOrderByOccurredAtDesc(UUID workspaceId);

	Optional<Transfer> findByIdAndWorkspaceId(UUID transferId, UUID workspaceId);

	@Query("""
			SELECT COALESCE(SUM(t.amount), 0)
			FROM Transfer t
			WHERE t.workspaceId = :workspaceId
			  AND t.toAccountId = :accountId
			""")
	BigDecimal sumIncomingByAccount(@Param("workspaceId") UUID workspaceId, @Param("accountId") UUID accountId);

	@Query("""
			SELECT COALESCE(SUM(t.amount), 0)
			FROM Transfer t
			WHERE t.workspaceId = :workspaceId
			  AND t.fromAccountId = :accountId
			""")
	BigDecimal sumOutgoingByAccount(@Param("workspaceId") UUID workspaceId, @Param("accountId") UUID accountId);
}
