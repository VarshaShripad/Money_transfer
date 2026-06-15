package com.fidelity.moneytransfer.service;

import com.fidelity.moneytransfer.domain.TransactionLog;

public interface RewardService {

    /**
     * Check if a transaction is eligible for rewards based on business rules.
     * Eligibility criteria:
     * 1. Transaction status is SUCCESS
     * 2. Transaction amount is greater than ₹100
     * 3. Sender and receiver are different users
     * 4. Transaction is not a self-transfer
     */
    boolean isEligibleForReward(TransactionLog transaction, Long fromUserId, Long toUserId);

    /**
     * Calculate reward points for an eligible transaction.
     * Logic: 1 reward point per ₹100 transferred (rounded down)
     * Example: ₹250 → 2 points, ₹99 → 0 points
     */
    Long calculateRewardPoints(java.math.BigDecimal amount);

    /**
     * Award rewards for a successful transaction.
     * This method handles the eligibility check and calculation internally.
     */
    void awardRewards(TransactionLog transaction, Long fromUserId, Long toUserId);

    /**
     * Get total reward points for a user
     */
    Long getTotalRewardPoints(Long userId);

    /**
     * Get all rewards earned by a user
     */
    java.util.List<com.fidelity.moneytransfer.domain.Reward> getUserRewards(Long userId);
}
