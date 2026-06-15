package com.fidelity.moneytransfer.dto;

import java.util.List;

public class RewardSummaryResponse {
    private Long userId;
    private Long totalRewardPoints;
    private List<RewardResponse> rewards;

    // Default constructor
    public RewardSummaryResponse() {}

    // Constructor
    public RewardSummaryResponse(Long userId, Long totalRewardPoints, List<RewardResponse> rewards) {
        this.userId = userId;
        this.totalRewardPoints = totalRewardPoints;
        this.rewards = rewards;
    }

    // Getters
    public Long getUserId() {
        return userId;
    }

    public Long getTotalRewardPoints() {
        return totalRewardPoints;
    }

    public List<RewardResponse> getRewards() {
        return rewards;
    }

    // Setters
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setTotalRewardPoints(Long totalRewardPoints) {
        this.totalRewardPoints = totalRewardPoints;
    }

    public void setRewards(List<RewardResponse> rewards) {
        this.rewards = rewards;
    }
}
