package com.example.superMalle.config;

import com.example.superMalle.entity.*;
import com.example.superMalle.entity.enums.OrderStatus;
import com.example.superMalle.entity.enums.OrderType;
import com.example.superMalle.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class EnhancedDataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final LoyaltyProgramRepository loyaltyProgramRepository;
    private final UserLoyaltyRepository userLoyaltyRepository;
    private final InventoryRepository inventoryRepository;
    private final OperatingHoursRepository operatingHoursRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@supermalle.com}")
    private String adminEmail;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting enhanced data initialization...");

        // Create default loyalty program if not exists
        createDefaultLoyaltyProgram();

        // Enroll admin in loyalty program if not enrolled
        enrollAdminInLoyalty();

        // Create sample inventory for some menu items
        createSampleInventory();

        // Seed default operating hours if not exists
        seedOperatingHours();

        log.info("Enhanced data initialization completed!");
    }

    private void createDefaultLoyaltyProgram() {
        if (loyaltyProgramRepository.findByIsActiveTrue().isEmpty()) {
            LoyaltyProgram program = LoyaltyProgram.builder()
                    .name("SuperMalle Rewards")
                    .description("Earn points with every order and redeem for discounts!")
                    .pointsPerDollar(10)
                    .redemptionRate(100)
                    .minPointsToRedeem(500)
                    .maxPointsPerOrder(1000)
                    .welcomeBonusPoints(100)
                    .referralBonusPoints(500)
                    .isActive(true)
                    .build();

            loyaltyProgramRepository.save(program);
            log.info("Created default loyalty program: SuperMalle Rewards");
        } else {
            log.info("Loyalty program already exists, skipping creation");
        }
    }

    private void enrollAdminInLoyalty() {
        User admin = userRepository.findByEmail(adminEmail).orElse(null);
        if (admin != null && userLoyaltyRepository.findByUser(admin).isEmpty()) {
            LoyaltyProgram program = loyaltyProgramRepository.findByIsActiveTrue()
                    .orElseThrow(() -> new RuntimeException("No active loyalty program found"));

            String referralCode = generateReferralCode();
            while (userLoyaltyRepository.existsByReferralCode(referralCode)) {
                referralCode = generateReferralCode();
            }

            UserLoyalty userLoyalty = UserLoyalty.builder()
                    .user(admin)
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

            userLoyaltyRepository.save(userLoyalty);
            log.info("Enrolled admin in loyalty program with referral code: {}", referralCode);
        }
    }

    private void createSampleInventory() {
        java.util.List<MenuItem> menuItems = menuItemRepository.findAll();

        for (MenuItem menuItem : menuItems) {
            if (!inventoryRepository.existsByMenuItem(menuItem)) {
                int quantity = (int) (Math.random() * 50) + 10;
                int reorderLevel = (int) (quantity * 0.2);

                Inventory inventory = Inventory.builder()
                        .menuItem(menuItem)
                        .quantity(quantity)
                        .reorderLevel(reorderLevel)
                        .maxQuantity(quantity * 2)
                        .unit("pieces")
                        .costPerUnit(menuItem.getPrice().doubleValue() * 0.4)
                        .supplierName("Default Supplier")
                        .supplierContact("supplier@example.com")
                        .nextRestockDate(LocalDateTime.now().plusDays(7))
                        .isActive(true)
                        .build();

                inventoryRepository.save(inventory);
                log.info("Created inventory for menu item: {} (quantity: {})", menuItem.getName(), quantity);
            }
        }

        log.info("Sample inventory creation completed");
    }

    private void seedOperatingHours() {
        if (operatingHoursRepository.count() > 0) {
            log.info("Operating hours already seeded, skipping");
            return;
        }
        operatingHoursRepository.save(OperatingHours.builder()
                .dayOfWeek(DayOfWeek.MONDAY).openTime(LocalTime.of(9, 0)).closeTime(LocalTime.of(22, 0)).build());
        operatingHoursRepository.save(OperatingHours.builder()
                .dayOfWeek(DayOfWeek.TUESDAY).openTime(LocalTime.of(9, 0)).closeTime(LocalTime.of(22, 0)).build());
        operatingHoursRepository.save(OperatingHours.builder()
                .dayOfWeek(DayOfWeek.WEDNESDAY).openTime(LocalTime.of(9, 0)).closeTime(LocalTime.of(22, 0)).build());
        operatingHoursRepository.save(OperatingHours.builder()
                .dayOfWeek(DayOfWeek.THURSDAY).openTime(LocalTime.of(9, 0)).closeTime(LocalTime.of(22, 0)).build());
        operatingHoursRepository.save(OperatingHours.builder()
                .dayOfWeek(DayOfWeek.FRIDAY).openTime(LocalTime.of(9, 0)).closeTime(LocalTime.of(23, 0)).build());
        operatingHoursRepository.save(OperatingHours.builder()
                .dayOfWeek(DayOfWeek.SATURDAY).openTime(LocalTime.of(10, 0)).closeTime(LocalTime.of(23, 0)).build());
        operatingHoursRepository.save(OperatingHours.builder()
                .dayOfWeek(DayOfWeek.SUNDAY).openTime(LocalTime.of(0, 0)).closeTime(LocalTime.of(23, 59)).build());
        log.info("Seeded operating hours for all 7 days");
    }

    private String generateReferralCode() {
        return java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
