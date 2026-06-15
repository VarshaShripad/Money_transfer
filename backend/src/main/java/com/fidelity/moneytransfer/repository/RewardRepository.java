package com.fidelity.moneytransfer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.fidelity.moneytransfer.domain.Reward;
import java.util.List;
import java.util.Optional;

@Repository
public interface RewardRepository extends JpaRepository<Reward, String> {

    /**
     * Find all rewards for a specific user (account owner)
     */
    List<Reward> findByUserId(Long userId);

    /**
     * Find reward by transaction ID
     */
    Optional<Reward> findByTransactionId(String transactionId);

    /**
     * Get total reward points for a user
     */
    @Query("SELECT COALESCE(SUM(r.rewardPoints), 0) FROM Reward r WHERE r.userId = :userId")
    Long getTotalRewardPoints(Long userId);
}
