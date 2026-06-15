package com.fidelity.moneytransfer.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.fidelity.moneytransfer.enums.TransactionStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "transaction_logs")
public class TransactionLog {

    @Id
    private String id; // UUID as String

    @Column(nullable = false)
    private Long fromAccountId;

    @Column(nullable = false)
    private Long toAccountId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private String failureReason;

    @Column(unique = true, nullable = false)
    private String idempotencyKey;

    private LocalDateTime createdOn;

    // ✅ REQUIRED by JPA
    public TransactionLog() {
    }

    // Optional constructor
    public TransactionLog(Long fromAccountId,
                          Long toAccountId,
                          BigDecimal amount,
                          TransactionStatus status,
                          String failureReason,
                          String idempotencyKey) {
        this.fromAccountId = fromAccountId;
        this.toAccountId = toAccountId;
        this.amount = amount;
        this.status = status;
        this.failureReason = failureReason;
        this.idempotencyKey = idempotencyKey;
    }

    @PrePersist
    public void prePersist() {
        this.id = UUID.randomUUID().toString();
        this.createdOn = LocalDateTime.now();
    }

    // ---------- Getters ----------

    public String getId() {
        return id;
    }

    public Long getFromAccountId() {
        return fromAccountId;
    }

    public Long getToAccountId() {
        return toAccountId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    // ---------- Setters ----------

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }
}
