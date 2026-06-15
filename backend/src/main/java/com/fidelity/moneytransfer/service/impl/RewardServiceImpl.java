package com.fidelity.moneytransfer.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fidelity.moneytransfer.domain.Reward;
import com.fidelity.moneytransfer.domain.TransactionLog;
import com.fidelity.moneytransfer.enums.TransactionStatus;
import com.fidelity.moneytransfer.repository.RewardRepository;
import com.fidelity.moneytransfer.service.RewardService;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RewardServiceImpl implements RewardService {

    private final RewardRepository rewardRepository;

    public RewardServiceImpl(RewardRepository rewardRepository) {
        this.rewardRepository = rewardRepository;
    }

    /**
     * Check if a transaction is eligible for rewards based on business rules
     */
    @Override
    public boolean isEligibleForReward(TransactionLog transaction, Long fromUserId, Long toUserId) {
        // Rule 1: Transaction status must be SUCCESS
        if (transaction.getStatus() != TransactionStatus.SUCCESS) {
            return false;
        }

        // Rule 2: Transaction amount must be greater than ₹100
        if (transaction.getAmount().compareTo(new BigDecimal("100")) <= 0) {
            return false;
        }

        // Rule 3 & 4: Sender and receiver must be different users (no self-transfer)
        if (fromUserId.equals(toUserId)) {
            return false;
        }

        // Already checked if it's from different accounts in TransferService,
        // but let's ensure account IDs also differ in the transaction log
        if (transaction.getFromAccountId().equals(transaction.getToAccountId())) {
            return false;
        }

        return true;
    }

    /**
     * Calculate reward points: 1 point per ₹100 transferred (rounded down)
     */
    @Override
    public Long calculateRewardPoints(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return 0L;
        }

        // Divide by 100 and round down
        BigDecimal pointsDecimal = amount.divide(new BigDecimal("100"), 0, java.math.RoundingMode.DOWN);
        return pointsDecimal.longValue();
    }

    /**
     * Award rewards for a successful transaction
     */
    @Override
    @Transactional
    public void awardRewards(TransactionLog transaction, Long fromUserId, Long toUserId) {
        // Check if eligible
        if (!isEligibleForReward(transaction, fromUserId, toUserId)) {
            return; // Not eligible, no reward
        }

        // Calculate reward points
        Long rewardPoints = calculateRewardPoints(transaction.getAmount());

        // Only award if points > 0
        if (rewardPoints > 0) {
            // Award reward to the sender (user who is performing the transfer)
            Reward reward = new Reward(fromUserId, transaction.getId(), rewardPoints);
            rewardRepository.save(reward);
        }
    }

    /**
     * Get total reward points for a user
     */
    @Override
    public Long getTotalRewardPoints(Long userId) {
        return rewardRepository.getTotalRewardPoints(userId);
    }

    /**
     * Get all rewards earned by a user
     */
    @Override
    public List<Reward> getUserRewards(Long userId) {
        return rewardRepository.findByUserId(userId);
    }
}
