
package com.financesuite.zsurapp.account.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.financesuite.zsurapp.account.entity.Account;

public interface AccountRepository
        extends JpaRepository<Account, UUID> {

    List<Account> findAllByWorkspaceId(UUID workspaceId);

    Optional<Account> findByIdAndWorkspaceId(
        UUID id,
        UUID workspaceId
    );
}
