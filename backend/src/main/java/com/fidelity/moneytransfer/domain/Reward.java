package com.fidelity.moneytransfer.domain;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "rewards")
public class Reward {

    @Id
    private String id; // UUID as String

    @Column(nullable = false)
    private Long userId; // Account ID of the user earning the reward

    @Column(nullable = false)
    private String transactionId; // Reference to the transaction log ID

    @Column(nullable = false)
    private Long rewardPoints; // Points earned from this transaction

    private LocalDateTime earnedOn;

    // ✅ REQUIRED by JPA
    public Reward() {
    }

    // Constructor
    public Reward(Long userId, String transactionId, Long rewardPoints) {
        this.userId = userId;
        this.transactionId = transactionId;
        this.rewardPoints = rewardPoints;
    }

    @PrePersist
    public void prePersist() {
        this.id = UUID.randomUUID().toString();
        this.earnedOn = LocalDateTime.now();
    }

    // ---------- Getters ----------
    public String getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public Long getRewardPoints() {
        return rewardPoints;
    }

    public LocalDateTime getEarnedOn() {
        return earnedOn;
    }

    // ---------- Setters ----------
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setRewardPoints(Long rewardPoints) {
        this.rewardPoints = rewardPoints;
    }
}
