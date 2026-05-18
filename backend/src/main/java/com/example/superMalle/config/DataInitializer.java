package com.example.superMalle.config;

import com.example.superMalle.entity.Category;
import com.example.superMalle.entity.MenuItem;
import com.example.superMalle.entity.enums.UserRole;
import com.example.superMalle.entity.User;
import com.example.superMalle.repository.CategoryRepository;
import com.example.superMalle.repository.MenuItemRepository;
import com.example.superMalle.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    @Override
    @Transactional
    public void run(String... args) {
        if (categoryRepository.count() > 0) {
            log.info("Data already seeded, skipping initialization");
            return;
        }

        log.info("Seeding database with initial data...");

        // Create admin user
        String adminEmail = env.getProperty("app.admin.email", "admin@supermalle.com");
        String adminPassword = env.getProperty("app.admin.password", "Admin@2026!");
        if (userRepository.findByEmail(adminEmail).isEmpty()) {
            User admin = User.builder()
                    .name("Admin")
                    .email(adminEmail)
                    .phone("0000000000")
                    .passwordHash(passwordEncoder.encode(adminPassword))
                    .role(UserRole.ADMIN)
                    .isActive(true)
                    .build();
            userRepository.save(admin);
            log.info("Created admin user: {} / {}", adminEmail, adminPassword);
        }

        // Create categories
        Category appetizers = saveCategory("Appetizers", "Start your meal right", 1);
        Category mains = saveCategory("Main Courses", "Hearty and satisfying", 2);
        Category pasta = saveCategory("Pasta", "Italian classics", 3);
        Category burgers = saveCategory("Burgers & Sandwiches", "All-American favorites", 4);
        Category salads = saveCategory("Salads", "Fresh and healthy", 5);
        Category desserts = saveCategory("Desserts", "Sweet endings", 6);
        Category drinks = saveCategory("Beverages", "Refreshing drinks", 7);
        Category sides = saveCategory("Sides", "Perfect accompaniments", 8);

        // Appetizers
        saveMenuItem("Crispy Calamari", "Lightly fried calamari with marinara sauce", new BigDecimal("9.99"), appetizers, "https://example.com/images/calamari.jpg");
        saveMenuItem("Bruschetta", "Toasted bread with fresh tomatoes, basil, and garlic", new BigDecimal("8.49"), appetizers, "https://example.com/images/bruschetta.jpg");
        saveMenuItem("Stuffed Mushrooms", "Mushroom caps stuffed with herbs and cheese", new BigDecimal("8.99"), appetizers, "https://example.com/images/mushrooms.jpg");
        saveMenuItem("Chicken Wings", "Crispy wings with your choice of sauce", new BigDecimal("11.99"), appetizers, "https://example.com/images/wings.jpg");
        saveMenuItem("Shrimp Cocktail", "Chilled jumbo shrimp with cocktail sauce", new BigDecimal("12.99"), appetizers, "https://example.com/images/shrimp-cocktail.jpg");

        // Main Courses
        saveMenuItem("Grilled Salmon", "Atlantic salmon with lemon butter sauce", new BigDecimal("22.99"), mains, "https://example.com/images/salmon.jpg");
        saveMenuItem("Ribeye Steak", "12oz ribeye cooked to your preference", new BigDecimal("28.99"), mains, "https://example.com/images/steak.jpg");
        saveMenuItem("Herb Roasted Chicken", "Half chicken with rosemary and thyme", new BigDecimal("18.99"), mains, "https://example.com/images/roasted-chicken.jpg");
        saveMenuItem("Lamb Chops", "Grilled lamb chops with mint sauce", new BigDecimal("26.99"), mains, "https://example.com/images/lamb-chops.jpg");
        saveMenuItem("Seafood Platter", "Grilled fish, shrimp, and scallops", new BigDecimal("32.99"), mains, "https://example.com/images/seafood-platter.jpg");

        // Pasta
        saveMenuItem("Spaghetti Carbonara", "Classic carbonara with pancetta and parmesan", new BigDecimal("16.99"), pasta, "https://example.com/images/carbonara.jpg");
        saveMenuItem("Fettuccine Alfredo", "Creamy alfredo sauce with fettuccine", new BigDecimal("15.49"), pasta, "https://example.com/images/alfredo.jpg");
        saveMenuItem("Penne Arrabbiata", "Spicy tomato sauce with penne", new BigDecimal("14.49"), pasta, "https://example.com/images/arrabbiata.jpg");
        saveMenuItem("Lasagna", "Layers of pasta, meat sauce, and cheese", new BigDecimal("17.99"), pasta, "https://example.com/images/lasagna.jpg");
        saveMenuItem("Pesto Gnocchi", "Potato gnocchi with fresh basil pesto", new BigDecimal("16.49"), pasta, "https://example.com/images/gnocchi.jpg");

        // Burgers & Sandwiches
        saveMenuItem("Classic Cheeseburger", "Angus beef with cheddar, lettuce, tomato", new BigDecimal("13.99"), burgers, "https://example.com/images/cheeseburger.jpg");
        saveMenuItem("BBQ Bacon Burger", "Smoky BBQ sauce, crispy bacon, and onion rings", new BigDecimal("15.99"), burgers, "https://example.com/images/bbq-burger.jpg");
        saveMenuItem("Grilled Chicken Sandwich", "Grilled chicken breast with avocado", new BigDecimal("13.49"), burgers, "https://example.com/images/chicken-sandwich.jpg");
        saveMenuItem("Philly Cheesesteak", "Shaved steak with peppers and provolone", new BigDecimal("14.99"), burgers, "https://example.com/images/philly.jpg");

        // Salads
        saveMenuItem("Caesar Salad", "Romaine lettuce, croutons, and parmesan", new BigDecimal("10.99"), salads, "https://example.com/images/caesar.jpg");
        saveMenuItem("Greek Salad", "Cucumbers, olives, feta, and tomatoes", new BigDecimal("11.49"), salads, "https://example.com/images/greek.jpg");
        saveMenuItem("Cobb Salad", "Chicken, bacon, egg, avocado, and blue cheese", new BigDecimal("13.99"), salads, "https://example.com/images/cobb.jpg");
        saveMenuItem("Mediterranean Bowl", "Quinoa, hummus, falafel, and tahini", new BigDecimal("12.99"), salads, "https://example.com/images/mediterranean.jpg");

        // Desserts
        saveMenuItem("Tiramisu", "Classic Italian coffee dessert", new BigDecimal("9.99"), desserts, "https://example.com/images/tiramisu.jpg");
        saveMenuItem("Chocolate Lava Cake", "Warm chocolate cake with molten center", new BigDecimal("10.99"), desserts, "https://example.com/images/lava-cake.jpg");
        saveMenuItem("Crème Brûlée", "Vanilla custard with caramelized sugar top", new BigDecimal("9.49"), desserts, "https://example.com/images/creme-brulee.jpg");
        saveMenuItem("Cheesecake", "New York style with berry compote", new BigDecimal("9.99"), desserts, "https://example.com/images/cheesecake.jpg");

        // Beverages
        saveMenuItem("Fresh Lemonade", "House-made lemonade with mint", new BigDecimal("4.99"), drinks, "https://example.com/images/lemonade.jpg");
        saveMenuItem("Iced Tea", "Classic iced tea with lemon", new BigDecimal("3.49"), drinks, "https://example.com/images/iced-tea.jpg");
        saveMenuItem("Craft Soda", "Artisanal soda in rotating flavors", new BigDecimal("3.99"), drinks, "https://example.com/images/soda.jpg");
        saveMenuItem("Espresso", "Double shot of premium espresso", new BigDecimal("3.99"), drinks, "https://example.com/images/espresso.jpg");
        saveMenuItem("Smoothie", "Mixed berry smoothie with yogurt", new BigDecimal("6.49"), drinks, "https://example.com/images/smoothie.jpg");

        // Sides
        saveMenuItem("French Fries", "Crispy golden fries with sea salt", new BigDecimal("4.99"), sides, "https://example.com/images/fries.jpg");
        saveMenuItem("Onion Rings", "Beer-battered onion rings", new BigDecimal("5.99"), sides, "https://example.com/images/onion-rings.jpg");
        saveMenuItem("Garlic Bread", "Toasted bread with garlic butter", new BigDecimal("4.49"), sides, "https://example.com/images/garlic-bread.jpg");
        saveMenuItem("Coleslaw", "Creamy coleslaw with carrots and cabbage", new BigDecimal("3.99"), sides, "https://example.com/images/coleslaw.jpg");
        saveMenuItem("Mashed Potatoes", "Creamy mashed potatoes with gravy", new BigDecimal("5.49"), sides, "https://example.com/images/mashed-potatoes.jpg");

        log.info("Seeded {} categories and {} menu items", categoryRepository.count(), menuItemRepository.count());
    }

    private Category saveCategory(String name, String description, int sortOrder) {
        Category category = Category.builder()
                .name(name)
                .description(description)
                .sortOrder(sortOrder)
                .isActive(true)
                .build();
        return categoryRepository.save(category);
    }

    private MenuItem saveMenuItem(String name, String description, BigDecimal price, Category category, String imageUrl) {
        MenuItem item = MenuItem.builder()
                .name(name)
                .description(description)
                .price(price)
                .category(category)
                .imageUrl(imageUrl)
                .isAvailable(true)
                .preparationTimeMinutes(15)
                .build();
        return menuItemRepository.save(item);
    }
}
