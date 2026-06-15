package com.fidelity.moneytransfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.fidelity.moneytransfer.domain.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
}
