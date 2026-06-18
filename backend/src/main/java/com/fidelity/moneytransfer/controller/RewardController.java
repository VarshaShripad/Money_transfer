package com.fidelity.moneytransfer.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.fidelity.moneytransfer.domain.Account;
import com.fidelity.moneytransfer.domain.Reward;
import com.fidelity.moneytransfer.dto.RewardResponse;
import com.fidelity.moneytransfer.dto.RewardSummaryResponse;
import com.fidelity.moneytransfer.repository.AccountRepository;
import com.fidelity.moneytransfer.service.RewardService;

@RestController
@RequestMapping("/api/v1/rewards")
public class RewardController {

    private final RewardService rewardService;
    private final AccountRepository accountRepository;

    public RewardController(
            RewardService rewardService,
            AccountRepository accountRepository) {
        this.rewardService = rewardService;
        this.accountRepository = accountRepository;
    }

    /**
     * Get total reward points for the authenticated user
     */
    @GetMapping("/total")
    public ResponseEntity<Long> getTotalRewardPoints() {
        Long userId = extractUserIdFromContext();
        Long totalPoints = rewardService.getTotalRewardPoints(userId);
        return ResponseEntity.ok(totalPoints);
    }

    /**
     * Get all rewards earned by the authenticated user
     */
    @GetMapping
    public ResponseEntity<List<RewardResponse>> getUserRewards() {
        Long userId = extractUserIdFromContext();

        List<Reward> rewards = rewardService.getUserRewards(userId);

        List<RewardResponse> rewardResponses = rewards.stream()
                .map(this::convertToRewardResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(rewardResponses);
    }

    /**
     * Get reward summary (total points + list of rewards)
     */
    @GetMapping("/summary")
    public ResponseEntity<RewardSummaryResponse> getRewardSummary() {
        Long userId = extractUserIdFromContext();

        Long totalPoints = rewardService.getTotalRewardPoints(userId);

        List<Reward> rewards = rewardService.getUserRewards(userId);

        List<RewardResponse> rewardResponses = rewards.stream()
                .map(this::convertToRewardResponse)
                .collect(Collectors.toList());

        RewardSummaryResponse summary =
                new RewardSummaryResponse(
                        userId,
                        totalPoints,
                        rewardResponses);

        return ResponseEntity.ok(summary);
    }

    /**
     * Get reward details by user ID (admin only or owner)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<RewardSummaryResponse> getUserRewardsByUserId(
            @PathVariable Long userId) {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        Long currentUserId = extractUserIdFromContext();

        if (!isAdmin && !currentUserId.equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        Long totalPoints = rewardService.getTotalRewardPoints(userId);

        List<Reward> rewards = rewardService.getUserRewards(userId);

        List<RewardResponse> rewardResponses = rewards.stream()
                .map(this::convertToRewardResponse)
                .collect(Collectors.toList());

        RewardSummaryResponse summary =
                new RewardSummaryResponse(
                        userId,
                        totalPoints,
                        rewardResponses);

        return ResponseEntity.ok(summary);
    }

    /**
     * Resolve logged-in user's account ID from username
     */
    private Long extractUserIdFromContext() {

        Authentication auth =
                SecurityContextHolder.getContext().getAuthentication();

        String username = auth.getName();

        Account account = accountRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Account not found for username: " + username));

        return account.getId();
    }

    /**
     * Convert Reward entity to DTO
     */
    private RewardResponse convertToRewardResponse(Reward reward) {
        return new RewardResponse(
                reward.getId(),
                reward.getUserId(),
                reward.getTransactionId(),
                reward.getRewardPoints(),
                reward.getEarnedOn()
        );
    }
}