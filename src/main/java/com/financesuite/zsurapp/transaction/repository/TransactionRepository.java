
package com.financesuite.zsurapp.transaction.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financesuite.zsurapp.transaction.entity.Transaction;

public interface TransactionRepository
        extends JpaRepository<Transaction, UUID> {

    List<Transaction> findAllByWorkspaceIdOrderByOccurredAtDesc(
            UUID workspaceId
    );

    List<Transaction> findAllByWorkspaceIdAndAccountIdOrderByOccurredAtDesc(
            UUID workspaceId,
            UUID accountId
    );

    Optional<Transaction> findByIdAndWorkspaceId(
            UUID transactionId,
            UUID workspaceId
    );
}
