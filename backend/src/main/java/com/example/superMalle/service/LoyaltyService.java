package com.example.superMalle.service;

import com.example.superMalle.dto.loyalty.*;
import com.example.superMalle.entity.*;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.exception.ResourceNotFoundException;
import com.example.superMalle.repository.*;
import com.example.superMalle.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoyaltyService {

    private final LoyaltyProgramRepository loyaltyProgramRepository;
    private final UserLoyaltyRepository userLoyaltyRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final EmailService emailService;

    // === Loyalty Program Management ===

    @Transactional
    public LoyaltyProgramResponse createLoyaltyProgram(LoyaltyProgramRequest request) {
        if (request == null) {
            throw new BadRequestException("Loyalty program request cannot be null");
        }

        if (loyaltyProgramRepository.existsByName(request.getName())) {
            throw new BadRequestException("Loyalty program with this name already exists");
        }

        LoyaltyProgram program = LoyaltyProgram.builder()
                .name(request.getName())
                .description(request.getDescription())
                .pointsPerDollar(request.getPointsPerDollar())
                .redemptionRate(request.getRedemptionRate())
                .minPointsToRedeem(request.getMinPointsToRedeem())
                .maxPointsPerOrder(request.getMaxPointsPerOrder())
                .welcomeBonusPoints(request.getWelcomeBonusPoints() != null ? request.getWelcomeBonusPoints() : 100)
                .referralBonusPoints(request.getReferralBonusPoints() != null ? request.getReferralBonusPoints() : 500)
                .isActive(true)
                .build();

        program = loyaltyProgramRepository.save(program);
        log.info("Created loyalty program: {}", program.getName());

        return toProgramResponse(program);
    }

    @Transactional
    public LoyaltyProgramResponse updateLoyaltyProgram(Long id, LoyaltyProgramRequest request) {
        if (id == null) {
            throw new BadRequestException("Loyalty program ID is required");
        }
        if (request == null) {
            throw new BadRequestException("Loyalty program request cannot be null");
        }

        LoyaltyProgram program = loyaltyProgramRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyProgram", "id", id));

        if (request.getName() != null && !request.getName().equals(program.getName())) {
            if (loyaltyProgramRepository.existsByName(request.getName())) {
                throw new BadRequestException("Loyalty program with this name already exists");
            }
            program.setName(request.getName());
        }
        if (request.getDescription() != null) {
            program.setDescription(request.getDescription());
        }
        if (request.getPointsPerDollar() != null) {
            program.setPointsPerDollar(request.getPointsPerDollar());
        }
        if (request.getRedemptionRate() != null) {
            program.setRedemptionRate(request.getRedemptionRate());
        }
        if (request.getMinPointsToRedeem() != null) {
            program.setMinPointsToRedeem(request.getMinPointsToRedeem());
        }
        if (request.getMaxPointsPerOrder() != null) {
            program.setMaxPointsPerOrder(request.getMaxPointsPerOrder());
        }
        if (request.getWelcomeBonusPoints() != null) {
            program.setWelcomeBonusPoints(request.getWelcomeBonusPoints());
        }
        if (request.getReferralBonusPoints() != null) {
            program.setReferralBonusPoints(request.getReferralBonusPoints());
        }

        program = loyaltyProgramRepository.save(program);
        log.info("Updated loyalty program: {}", program.getName());

        return toProgramResponse(program);
    }

    @Transactional
    public void deleteLoyaltyProgram(Long id) {
        if (id == null) {
            throw new BadRequestException("Loyalty program ID is required");
        }

        LoyaltyProgram program = loyaltyProgramRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("LoyaltyProgram", "id", id));

        program.setIsActive(false);
        loyaltyProgramRepository.save(program);

        log.info("Deleted loyalty program: {}", program.getName());
    }

    public LoyaltyProgramResponse getActiveLoyaltyProgram() {
        LoyaltyProgram program = loyaltyProgramRepository.findByIsActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Active Loyalty Program", "status", "active"));

        return toProgramResponse(program);
    }

    public List<LoyaltyProgramResponse> getAllLoyaltyPrograms() {
        return loyaltyProgramRepository.findByIsActiveTrueOrderByNameAsc().stream()
                .map(this::toProgramResponse)
                .collect(Collectors.toList());
    }

    // === User Loyalty Management ===

    @Transactional
    public UserLoyaltyResponse enrollUserInLoyalty(Long userId) {
        if (userId == null) {
            throw new BadRequestException("User ID is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (userLoyaltyRepository.existsByUser(user)) {
            throw new BadRequestException("User is already enrolled in loyalty program");
        }

        LoyaltyProgram program = loyaltyProgramRepository.findByIsActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Active Loyalty Program", "status", "active"));

        // Generate unique referral code
        String referralCode = generateReferralCode();
        while (userLoyaltyRepository.existsByReferralCode(referralCode)) {
            referralCode = generateReferralCode();
        }

        UserLoyalty userLoyalty = UserLoyalty.builder()
                .user(user)
                .loyaltyProgram(program)
                .totalPoints(program.getWelcomeBonusPoints())
                .availablePoints(program.getWelcomeBonusPoints())
                .redeemedPoints(0)
                .tierLevel("BRONZE")
                .lifetimePoints(program.getWelcomeBonusPoints())
                .totalOrders(0)
                .totalSpent(0.0)
                .referralCode(referralCode)
                .referralCount(0)
                .isActive(true)
                .build();

        userLoyalty = userLoyaltyRepository.save(userLoyalty);

        // Create welcome bonus transaction
        if (program.getWelcomeBonusPoints() > 0) {
            LoyaltyTransaction transaction = LoyaltyTransaction.createWelcomeBonusTransaction(
                    user, program, program.getWelcomeBonusPoints(), 0, program.getWelcomeBonusPoints());
            loyaltyTransactionRepository.save(transaction);
        }

        try {
            emailService.sendLoyaltyPointsEarned(
                    user.getEmail(),
                    user.getName(),
                    program.getWelcomeBonusPoints(),
                    userLoyalty.getAvailablePoints()
            );
        } catch (Exception e) {
            log.warn("Failed to send loyalty points email to {}: {}", user.getEmail(), e.getMessage());
        }

        log.info("Enrolled user {} in loyalty program with referral code: {}", user.getEmail(), referralCode);

        return toUserLoyaltyResponse(userLoyalty);
    }

    @Transactional
    public UserLoyaltyResponse applyReferralCode(Long userId, String referralCode) {
        if (userId == null) {
            throw new BadRequestException("User ID is required");
        }
        if (referralCode == null || referralCode.isBlank()) {
            throw new BadRequestException("Referral code is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (userLoyaltyRepository.existsByUser(user)) {
            throw new BadRequestException("User is already enrolled in loyalty program");
        }

        UserLoyalty referrerLoyalty = userLoyaltyRepository.findByReferralCode(referralCode)
                .orElseThrow(() -> new BadRequestException("Invalid referral code"));

        LoyaltyProgram program = loyaltyProgramRepository.findByIsActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Active Loyalty Program", "status", "active"));

        // Enroll the new user
        UserLoyalty newUserLoyalty = UserLoyalty.builder()
                .user(user)
                .loyaltyProgram(program)
                .totalPoints(program.getWelcomeBonusPoints())
                .availablePoints(program.getWelcomeBonusPoints())
                .redeemedPoints(0)
                .tierLevel("BRONZE")
                .lifetimePoints(program.getWelcomeBonusPoints())
                .totalOrders(0)
                .totalSpent(0.0)
                .referralCode(generateReferralCode())
                .referredBy(referralCode)
                .referralCount(0)
                .isActive(true)
                .build();

        newUserLoyalty = userLoyaltyRepository.save(newUserLoyalty);

        // Create welcome bonus transaction for new user
        if (program.getWelcomeBonusPoints() > 0) {
            LoyaltyTransaction transaction = LoyaltyTransaction.createWelcomeBonusTransaction(
                    user, program, program.getWelcomeBonusPoints(), 0, program.getWelcomeBonusPoints());
            loyaltyTransactionRepository.save(transaction);
        }

        // Award referral bonus to referrer
        if (program.getReferralBonusPoints() > 0) {
            Integer balanceBefore = referrerLoyalty.getAvailablePoints();
            referrerLoyalty.addPoints(program.getReferralBonusPoints());
            referrerLoyalty.setReferralCount(referrerLoyalty.getReferralCount() + 1);
            referrerLoyalty = userLoyaltyRepository.save(referrerLoyalty);

            LoyaltyTransaction referralTransaction = LoyaltyTransaction.createReferralBonusTransaction(
                    referrerLoyalty.getUser(), program, program.getReferralBonusPoints(),
                    balanceBefore, referrerLoyalty.getAvailablePoints(), referralCode);
            loyaltyTransactionRepository.save(referralTransaction);
        }

        log.info("User {} applied referral code {} from user {}", user.getEmail(), referralCode, referrerLoyalty.getUser().getEmail());

        return toUserLoyaltyResponse(newUserLoyalty);
    }

    public UserLoyaltyResponse getUserLoyalty(Long userId) {
        if (userId == null) {
            throw new BadRequestException("User ID is required");
        }

        UserLoyalty userLoyalty = userLoyaltyRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserLoyalty", "userId", userId));

        return toUserLoyaltyResponse(userLoyalty);
    }

    public UserLoyaltyResponse getMyLoyalty() {
        Long userId = getAuthenticatedUserId();
        return getUserLoyalty(userId);
    }

    public List<UserLoyaltyResponse> getLeaderboard(int limit) {
        if (limit < 1) limit = 10;
        if (limit > 100) limit = 100;

        Pageable pageable = PageRequest.of(0, limit, Sort.by("totalPoints").descending());
        return userLoyaltyRepository.findTopByTotalPointsDesc(pageable).stream()
                .map(this::toUserLoyaltyResponse)
                .collect(Collectors.toList());
    }

    // === Points Management ===

    @Transactional
    public UserLoyaltyResponse redeemPoints(RedeemPointsRequest request) {
        if (request == null || request.getPoints() == null) {
            throw new BadRequestException("Points to redeem is required");
        }

        Long userId = getAuthenticatedUserId();
        UserLoyalty userLoyalty = userLoyaltyRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("UserLoyalty", "userId", userId));

        LoyaltyProgram program = loyaltyProgramRepository.findByIsActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Active Loyalty Program", "status", "active"));

        if (!userLoyalty.canRedeemPoints(request.getPoints())) {
            throw new BadRequestException("Insufficient points balance");
        }

        if (request.getPoints() < program.getMinPointsToRedeem()) {
            throw new BadRequestException("Minimum points to redeem is " + program.getMinPointsToRedeem());
        }

        if (program.getMaxPointsPerOrder() != null && request.getPoints() > program.getMaxPointsPerOrder()) {
            throw new BadRequestException("Maximum points per order is " + program.getMaxPointsPerOrder());
        }

        Integer balanceBefore = userLoyalty.getAvailablePoints();
        userLoyalty.redeemPoints(request.getPoints());
        userLoyalty = userLoyaltyRepository.save(userLoyalty);

        // Create redemption transaction
        Order order = null;
        if (request.getOrderId() != null) {
            order = orderRepository.findById(request.getOrderId()).orElse(null);
        }

        LoyaltyTransaction transaction = LoyaltyTransaction.createRedeemedTransaction(
                userLoyalty.getUser(), program, order, request.getPoints(), balanceBefore, userLoyalty.getAvailablePoints());
        loyaltyTransactionRepository.save(transaction);

        log.info("User {} redeemed {} points", userLoyalty.getUser().getEmail(), request.getPoints());

        return toUserLoyaltyResponse(userLoyalty);
    }

    @Transactional
    public void awardPointsForOrder(Long orderId) {
        if (orderId == null) {
            throw new BadRequestException("Order ID is required");
        }

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() != com.example.superMalle.entity.enums.OrderStatus.COMPLETED) {
            throw new BadRequestException("Can only award points for completed orders");
        }

        UserLoyalty userLoyalty = userLoyaltyRepository.findByUserId(order.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("UserLoyalty", "userId", order.getUser().getId()));

        LoyaltyProgram program = loyaltyProgramRepository.findByIsActiveTrue()
                .orElseThrow(() -> new ResourceNotFoundException("Active Loyalty Program", "status", "active"));

        // Calculate points with tier multiplier
        Integer basePoints = program.calculatePointsEarned(order.getTotalAmount().doubleValue());
        Double tierMultiplier = userLoyalty.getTierMultiplier();
        Integer pointsEarned = (int) (basePoints * tierMultiplier);

        Integer balanceBefore = userLoyalty.getAvailablePoints();
        userLoyalty.addPoints(pointsEarned);
        userLoyalty.setTotalOrders(userLoyalty.getTotalOrders() + 1);
        userLoyalty.setTotalSpent(userLoyalty.getTotalSpent() + order.getTotalAmount().doubleValue());
        userLoyalty.setLastOrderDate(LocalDateTime.now());
        userLoyalty = userLoyaltyRepository.save(userLoyalty);

        // Create earned transaction
        LoyaltyTransaction transaction = LoyaltyTransaction.createEarnedTransaction(
                userLoyalty.getUser(), program, order, pointsEarned, balanceBefore, userLoyalty.getAvailablePoints());
        loyaltyTransactionRepository.save(transaction);

        try {
            emailService.sendLoyaltyPointsEarned(
                    userLoyalty.getUser().getEmail(),
                    userLoyalty.getUser().getName(),
                    pointsEarned,
                    userLoyalty.getAvailablePoints()
            );
        } catch (Exception e) {
            log.warn("Failed to send loyalty points email to {}: {}", userLoyalty.getUser().getEmail(), e.getMessage());
        }

        log.info("Awarded {} points to user {} for order {}", pointsEarned, userLoyalty.getUser().getEmail(), order.getOrderNumber());
    }

    // === Transaction History ===

    public List<LoyaltyTransactionResponse> getMyTransactionHistory() {
        Long userId = getAuthenticatedUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return loyaltyTransactionRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }

    public List<LoyaltyTransactionResponse> getUserTransactionHistory(Long userId) {
        if (userId == null) {
            throw new BadRequestException("User ID is required");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        return loyaltyTransactionRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }

    // === Helper Methods ===

    private String generateReferralCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public Long getAuthenticatedUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getId();
            }
        } catch (Exception e) {
            log.warn("Could not get authenticated user ID", e);
        }
        throw new BadRequestException("Could not authenticate user");
    }

    private LoyaltyProgramResponse toProgramResponse(LoyaltyProgram program) {
        return LoyaltyProgramResponse.builder()
                .id(program.getId())
                .name(program.getName())
                .description(program.getDescription())
                .pointsPerDollar(program.getPointsPerDollar())
                .redemptionRate(program.getRedemptionRate())
                .minPointsToRedeem(program.getMinPointsToRedeem())
                .maxPointsPerOrder(program.getMaxPointsPerOrder())
                .welcomeBonusPoints(program.getWelcomeBonusPoints())
                .referralBonusPoints(program.getReferralBonusPoints())
                .isActive(program.getIsActive())
                .createdAt(program.getCreatedAt())
                .updatedAt(program.getUpdatedAt())
                .redemptionRateDisplay(program.getRedemptionRate() + " points = $" + (program.getRedemptionRate() / 100.0))
                .build();
    }

    private UserLoyaltyResponse toUserLoyaltyResponse(UserLoyalty userLoyalty) {
        LoyaltyProgram program = userLoyalty.getLoyaltyProgram();
        Double discountValue = program.calculateDiscountValue(userLoyalty.getAvailablePoints());

        return UserLoyaltyResponse.builder()
                .id(userLoyalty.getId())
                .userId(userLoyalty.getUser().getId())
                .userName(userLoyalty.getUser().getName())
                .userEmail(userLoyalty.getUser().getEmail())
                .loyaltyProgramId(program.getId())
                .loyaltyProgramName(program.getName())
                .totalPoints(userLoyalty.getTotalPoints())
                .availablePoints(userLoyalty.getAvailablePoints())
                .redeemedPoints(userLoyalty.getRedeemedPoints())
                .tierLevel(userLoyalty.getTierLevel())
                .lifetimePoints(userLoyalty.getLifetimePoints())
                .totalOrders(userLoyalty.getTotalOrders())
                .totalSpent(userLoyalty.getTotalSpent())
                .lastOrderDate(userLoyalty.getLastOrderDate())
                .referralCode(userLoyalty.getReferralCode())
                .referredBy(userLoyalty.getReferredBy())
                .referralCount(userLoyalty.getReferralCount())
                .isActive(userLoyalty.getIsActive())
                .createdAt(userLoyalty.getCreatedAt())
                .updatedAt(userLoyalty.getUpdatedAt())
                .tierMultiplier(userLoyalty.getTierMultiplier())
                .tierBenefits(getTierBenefits(userLoyalty.getTierLevel()))
                .pointsToNextTier(calculatePointsToNextTier(userLoyalty.getLifetimePoints()))
                .nextTier(getNextTier(userLoyalty.getTierLevel()))
                .discountValue(discountValue)
                .build();
    }

    private LoyaltyTransactionResponse toTransactionResponse(LoyaltyTransaction transaction) {
        return LoyaltyTransactionResponse.builder()
                .id(transaction.getId())
                .userId(transaction.getUser().getId())
                .userName(transaction.getUser().getName())
                .loyaltyProgramId(transaction.getLoyaltyProgram().getId())
                .loyaltyProgramName(transaction.getLoyaltyProgram().getName())
                .orderId(transaction.getOrder() != null ? transaction.getOrder().getId() : null)
                .orderNumber(transaction.getOrder() != null ? transaction.getOrder().getOrderNumber() : null)
                .transactionType(transaction.getTransactionType())
                .points(transaction.getPoints())
                .balanceBefore(transaction.getBalanceBefore())
                .balanceAfter(transaction.getBalanceAfter())
                .description(transaction.getDescription())
                .referenceId(transaction.getReferenceId())
                .createdAt(transaction.getCreatedAt())
                .isPositive(transaction.getPoints() > 0)
                .build();
    }

    private String getTierBenefits(String tierLevel) {
        return switch (tierLevel) {
            case "PLATINUM" -> "1.5x points multiplier, priority support, exclusive offers";
            case "GOLD" -> "1.25x points multiplier, priority support";
            case "SILVER" -> "1.1x points multiplier";
            default -> "Standard benefits";
        };
    }

    private Integer calculatePointsToNextTier(Integer lifetimePoints) {
        if (lifetimePoints >= 10000) return 0;
        if (lifetimePoints >= 5000) return 10000 - lifetimePoints;
        if (lifetimePoints >= 1000) return 5000 - lifetimePoints;
        return 1000 - lifetimePoints;
    }

    private String getNextTier(String currentTier) {
        return switch (currentTier) {
            case "BRONZE" -> "SILVER";
            case "SILVER" -> "GOLD";
            case "GOLD" -> "PLATINUM";
            default -> "MAX";
        };
    }
}
