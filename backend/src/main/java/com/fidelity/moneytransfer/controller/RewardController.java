package com.fidelity.moneytransfer.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import com.fidelity.moneytransfer.domain.Reward;
import com.fidelity.moneytransfer.dto.RewardResponse;
import com.fidelity.moneytransfer.dto.RewardSummaryResponse;
import com.fidelity.moneytransfer.service.RewardService;

@RestController
@RequestMapping("/api/v1/rewards")
public class RewardController {

    private final RewardService rewardService;

    public RewardController(RewardService rewardService) {
        this.rewardService = rewardService;
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
     * Get reward summary (total points + list of all rewards) for the authenticated user
     */
    @GetMapping("/summary")
    public ResponseEntity<RewardSummaryResponse> getRewardSummary() {
        Long userId = extractUserIdFromContext();
        Long totalPoints = rewardService.getTotalRewardPoints(userId);
        List<Reward> rewards = rewardService.getUserRewards(userId);
        List<RewardResponse> rewardResponses = rewards.stream()
                .map(this::convertToRewardResponse)
                .collect(Collectors.toList());
        
        RewardSummaryResponse summary = new RewardSummaryResponse(userId, totalPoints, rewardResponses);
        return ResponseEntity.ok(summary);
    }

    /**
     * Get reward details by user ID (admin only or for own account)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<RewardSummaryResponse> getUserRewardsByUserId(@PathVariable Long userId) {
        // Authorization: user can only see their own rewards, admin can see anyone's
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        
        Long currentUserId = extractUserIdFromContext();
        if (!isAdmin && !currentUserId.equals(userId)) {
            return ResponseEntity.status(403).build(); // Forbidden
        }

        Long totalPoints = rewardService.getTotalRewardPoints(userId);
        List<Reward> rewards = rewardService.getUserRewards(userId);
        List<RewardResponse> rewardResponses = rewards.stream()
                .map(this::convertToRewardResponse)
                .collect(Collectors.toList());
        
        RewardSummaryResponse summary = new RewardSummaryResponse(userId, totalPoints, rewardResponses);
        return ResponseEntity.ok(summary);
    }

    /**
     * Helper method to extract user ID from security context
     * Note: This assumes the account ID is stored as the principal or in claims
     * Adjust based on your authentication implementation
     */
    private Long extractUserIdFromContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        // For now, returning a placeholder - this should be implemented based on your auth system
        // This typically would fetch the account ID based on username
        // TODO: Implement proper user-to-account mapping
        return 1L; // Placeholder - needs proper implementation
    }

    /**
     * Helper method to convert Reward entity to RewardResponse DTO
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
