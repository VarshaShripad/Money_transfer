package com.fidelity.moneytransfer.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fidelity.moneytransfer.domain.TransactionLog;

public interface TransactionLogRepository extends JpaRepository<TransactionLog, String> {

    Optional<TransactionLog> findByIdempotencyKey(String idempotencyKey);
}
