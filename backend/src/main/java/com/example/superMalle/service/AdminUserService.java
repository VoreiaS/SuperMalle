package com.example.superMalle.service;

import com.example.superMalle.dto.admin.AdminResetPasswordRequest;
import com.example.superMalle.dto.admin.AdminUserRequest;
import com.example.superMalle.dto.admin.AdminUserResponse;
import com.example.superMalle.dto.menu.PagedResponse;
import com.example.superMalle.entity.User;
import com.example.superMalle.entity.enums.UserRole;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.exception.ResourceNotFoundException;
import com.example.superMalle.repository.OrderRepository;
import com.example.superMalle.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PagedResponse<AdminUserResponse> getAllUsers(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage;

        if (search != null && !search.isBlank()) {
            String term = "%" + search.toLowerCase() + "%";
            userPage = userRepository.searchUsers(term, pageable);
        } else {
            userPage = userRepository.findAll(pageable);
        }

        var responses = userPage.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.<AdminUserResponse>builder()
                .items(responses)
                .total(userPage.getTotalElements())
                .page(page)
                .size(size)
                .totalPages(userPage.getTotalPages())
                .build();
    }

    public AdminUserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        return toResponse(user);
    }

    @Transactional
    public AdminUserResponse updateUser(Long id, AdminUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));

        if (request.getName() != null) user.setName(request.getName());
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email already in use");
            }
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new BadRequestException("Phone already in use");
            }
            user.setPhone(request.getPhone());
        }
        if (request.getRole() != null) {
            user.setRole(UserRole.valueOf(request.getRole()));
        }
        if (request.getIsActive() != null) {
            user.setIsActive(request.getIsActive());
        }

        user = userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public AdminUserResponse createUser(AdminUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already in use");
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()
                && userRepository.existsByPhone(request.getPhone())) {
            throw new BadRequestException("Phone already in use");
        }

        String rawPassword;
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            rawPassword = request.getPassword();
        } else {
            rawPassword = generateSecurePassword();
        }

        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .passwordHash(passwordEncoder.encode(rawPassword))
                .role(request.getRole() != null ? UserRole.valueOf(request.getRole()) : UserRole.CUSTOMER)
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();

        user = userRepository.save(user);

        try {
            emailService.sendWelcomeEmail(user.getEmail(), user.getName());
        } catch (Exception e) {
            log.warn("Failed to send welcome email to {}: {}", user.getEmail(), e.getMessage());
        }

        String initialPw = request.getPassword() != null && !request.getPassword().isBlank() ? null : rawPassword;
        return toResponseWithPassword(user, initialPw);
    }

    @Transactional
    public AdminUserResponse toggleActive(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setIsActive(!user.getIsActive());
        user = userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        orderRepository.findByUserId(id).forEach(order -> {
            order.setUser(null);
            orderRepository.save(order);
        });
        userRepository.delete(user);
    }

    @Transactional
    public void resetPassword(Long id, AdminResetPasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    private AdminUserResponse toResponse(User user) {
        return toResponseWithPassword(user, null);
    }

    private AdminUserResponse toResponseWithPassword(User user, String initialPassword) {
        long orderCount = orderRepository.countByUserId(user.getId());
        return AdminUserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .orderCount(orderCount)
                .initialPassword(initialPassword)
                .build();
    }

    private String generateSecurePassword() {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String digits = "0123456789";
        String special = "!@#$%^&*()_+-=[]{}|;:,.<>?";
        String all = upper + lower + digits + special;
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(14);
        sb.append(upper.charAt(random.nextInt(upper.length())));
        sb.append(lower.charAt(random.nextInt(lower.length())));
        sb.append(digits.charAt(random.nextInt(digits.length())));
        sb.append(special.charAt(random.nextInt(special.length())));
        for (int i = 4; i < 14; i++) {
            sb.append(all.charAt(random.nextInt(all.length())));
        }
        // shuffle
        char[] chars = sb.toString().toCharArray();
        for (int i = chars.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char tmp = chars[i];
            chars[i] = chars[j];
            chars[j] = tmp;
        }
        return new String(chars);
    }
}
