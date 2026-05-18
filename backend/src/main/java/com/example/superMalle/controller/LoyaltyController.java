package com.example.superMalle.controller;

import com.example.superMalle.dto.loyalty.*;
import com.example.superMalle.service.LoyaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService loyaltyService;

    // === Customer Endpoints ===

    @GetMapping("/me")
    public ResponseEntity<UserLoyaltyResponse> getMyLoyalty() {
        return ResponseEntity.ok(loyaltyService.getMyLoyalty());
    }

    @PostMapping("/me/redeem")
    public ResponseEntity<UserLoyaltyResponse> redeemPoints(@Valid @RequestBody RedeemPointsRequest request) {
        return ResponseEntity.ok(loyaltyService.redeemPoints(request));
    }

    @GetMapping("/me/transactions")
    public ResponseEntity<List<LoyaltyTransactionResponse>> getMyTransactionHistory() {
        return ResponseEntity.ok(loyaltyService.getMyTransactionHistory());
    }

    @PostMapping("/enroll")
    public ResponseEntity<UserLoyaltyResponse> enrollInLoyalty() {
        Long userId = loyaltyService.getAuthenticatedUserId();
        return ResponseEntity.ok(loyaltyService.enrollUserInLoyalty(userId));
    }

    @PostMapping("/apply-referral")
    public ResponseEntity<UserLoyaltyResponse> applyReferralCode(@RequestParam String referralCode) {
        Long userId = loyaltyService.getAuthenticatedUserId();
        return ResponseEntity.ok(loyaltyService.applyReferralCode(userId, referralCode));
    }

    // === Public Endpoints ===

    @GetMapping("/program")
    public ResponseEntity<LoyaltyProgramResponse> getActiveLoyaltyProgram() {
        return ResponseEntity.ok(loyaltyService.getActiveLoyaltyProgram());
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<UserLoyaltyResponse>> getLeaderboard(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(loyaltyService.getLeaderboard(limit));
    }

    // === Admin Endpoints ===

    @GetMapping("/programs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LoyaltyProgramResponse>> getAllLoyaltyPrograms() {
        return ResponseEntity.ok(loyaltyService.getAllLoyaltyPrograms());
    }

    @PostMapping("/programs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoyaltyProgramResponse> createLoyaltyProgram(@Valid @RequestBody LoyaltyProgramRequest request) {
        return ResponseEntity.ok(loyaltyService.createLoyaltyProgram(request));
    }

    @PutMapping("/programs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LoyaltyProgramResponse> updateLoyaltyProgram(
            @PathVariable Long id,
            @Valid @RequestBody LoyaltyProgramRequest request) {
        return ResponseEntity.ok(loyaltyService.updateLoyaltyProgram(id, request));
    }

    @DeleteMapping("/programs/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLoyaltyProgram(@PathVariable Long id) {
        loyaltyService.deleteLoyaltyProgram(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserLoyaltyResponse> getUserLoyalty(@PathVariable Long userId) {
        return ResponseEntity.ok(loyaltyService.getUserLoyalty(userId));
    }

    @GetMapping("/users/{userId}/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<LoyaltyTransactionResponse>> getUserTransactionHistory(@PathVariable Long userId) {
        return ResponseEntity.ok(loyaltyService.getUserTransactionHistory(userId));
    }

    @PostMapping("/users/{userId}/enroll")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserLoyaltyResponse> enrollUserInLoyalty(@PathVariable Long userId) {
        return ResponseEntity.ok(loyaltyService.enrollUserInLoyalty(userId));
    }

    @PostMapping("/orders/{orderId}/award-points")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> awardPointsForOrder(@PathVariable Long orderId) {
        loyaltyService.awardPointsForOrder(orderId);
        return ResponseEntity.ok().build();
    }
}
