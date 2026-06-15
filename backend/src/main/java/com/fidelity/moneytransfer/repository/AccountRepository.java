package com.fidelity.moneytransfer.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.fidelity.moneytransfer.domain.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByIdAndUsername(Long id, String username);

    Optional<Account> findByUsername(String username);
}
