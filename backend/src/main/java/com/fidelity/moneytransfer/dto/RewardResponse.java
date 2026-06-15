package com.fidelity.moneytransfer.dto;

import java.time.LocalDateTime;

public class RewardResponse {
    private String id;
    private Long userId;
    private String transactionId;
    private Long rewardPoints;
    private LocalDateTime earnedOn;

    // Default constructor
    public RewardResponse() {}

    // Constructor
    public RewardResponse(String id, Long userId, String transactionId, Long rewardPoints, LocalDateTime earnedOn) {
        this.id = id;
        this.userId = userId;
        this.transactionId = transactionId;
        this.rewardPoints = rewardPoints;
        this.earnedOn = earnedOn;
    }

    // Getters
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

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public void setRewardPoints(Long rewardPoints) {
        this.rewardPoints = rewardPoints;
    }

    public void setEarnedOn(LocalDateTime earnedOn) {
        this.earnedOn = earnedOn;
    }
}
