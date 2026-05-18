package com.example.superMalle.service;

import com.example.superMalle.dto.auth.*;
import com.example.superMalle.entity.PasswordResetToken;
import com.example.superMalle.entity.RefreshToken;
import com.example.superMalle.entity.User;
import com.example.superMalle.entity.enums.UserRole;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.repository.PasswordResetTokenRepository;
import com.example.superMalle.repository.RefreshTokenRepository;
import com.example.superMalle.repository.UserRepository;
import com.example.superMalle.security.CustomUserDetails;
import com.example.superMalle.security.JwtUtil;
import com.example.superMalle.security.LoginRateLimitService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private LoginRateLimitService loginRateLimitService;
    @Mock
    private EmailService emailService;

    private AuthService authService;

    private User testUser;
    private final Long testUserId = 1L;
    private final String testEmail = "test@example.com";
    private final String testPassword = "password123";
    private final String testName = "Test User";

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordResetTokenRepository,
                refreshTokenRepository, passwordEncoder, jwtUtil,
                authenticationManager, loginRateLimitService, emailService);

        testUser = User.builder()
                .id(testUserId)
                .name(testName)
                .email(testEmail)
                .passwordHash("$2a$12$encodedHash")
                .role(UserRole.CUSTOMER)
                .isActive(true)
                .failedLoginAttempts(0)
                .build();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // === Register ===

    @Test
    @DisplayName("Should register a new user successfully")
    void shouldRegisterSuccessfully() {
        RegisterRequest request = new RegisterRequest();
        request.setName(testName);
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(passwordEncoder.encode(testPassword)).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(any(CustomUserDetails.class), eq(testUserId), eq(UserRole.CUSTOMER.name())))
                .thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getEmail()).isEqualTo(testEmail);
        assertThat(response.getUserId()).isEqualTo(testUserId);
        assertThat(response.getRole()).isEqualTo(UserRole.CUSTOMER.name());
        verify(emailService).sendWelcomeEmail(eq(testEmail), eq(testName));
    }

    @Test
    @DisplayName("Should throw when registering with blank email")
    void shouldThrowOnBlankEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setName(testName);
        request.setEmail("");
        request.setPassword(testPassword);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email is required");
    }

    @Test
    @DisplayName("Should throw when registering with duplicate email")
    void shouldThrowOnDuplicateEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setName(testName);
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        when(userRepository.existsByEmail(testEmail)).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Email already registered");
    }

    @Test
    @DisplayName("Should throw when registering with duplicate phone")
    void shouldThrowOnDuplicatePhone() {
        RegisterRequest request = new RegisterRequest();
        request.setName(testName);
        request.setEmail("other@example.com");
        request.setPhone("+1234567890");
        request.setPassword(testPassword);

        when(userRepository.existsByEmail("other@example.com")).thenReturn(false);
        when(userRepository.existsByPhone("+1234567890")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Phone number already in use");
    }

    @Test
    @DisplayName("Should register even if welcome email fails")
    void shouldRegisterWhenEmailFails() {
        RegisterRequest request = new RegisterRequest();
        request.setName(testName);
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        when(userRepository.existsByEmail(testEmail)).thenReturn(false);
        when(passwordEncoder.encode(testPassword)).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(any(CustomUserDetails.class), eq(testUserId), eq(UserRole.CUSTOMER.name())))
                .thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
        doThrow(new RuntimeException("SMTP down")).when(emailService).sendWelcomeEmail(any(), any());

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("access-token");
    }

    // === Login ===

    @Test
    @DisplayName("Should login successfully with valid credentials")
    void shouldLoginSuccessfully() {
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        when(loginRateLimitService.tryConsume("127.0.0.1")).thenReturn(true);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userRepository.findByEmail(userDetails.getUsername())).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(any(CustomUserDetails.class), eq(testUserId), eq(UserRole.CUSTOMER.name())))
                .thenReturn("access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        AuthResponse response = authService.login(request, "127.0.0.1");

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("access-token");
        assertThat(response.getEmail()).isEqualTo(testEmail);
        verify(loginRateLimitService).resetLimit("127.0.0.1");
    }

    @Test
    @DisplayName("Should throw when login rate limited")
    void shouldThrowOnRateLimit() {
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        when(loginRateLimitService.tryConsume("127.0.0.1")).thenReturn(false);
        when(loginRateLimitService.getRemainingAttempts("127.0.0.1")).thenReturn(0);

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Too many login attempts");
    }

    @Test
    @DisplayName("Should throw when account is locked")
    void shouldThrowOnLockedAccount() {
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword(testPassword);

        testUser.setLockedUntil(LocalDateTime.now().plusMinutes(15));

        when(loginRateLimitService.tryConsume("127.0.0.1")).thenReturn(true);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Account locked");
    }

    @Test
    @DisplayName("Should track failed login attempts")
    void shouldTrackFailedLoginAttempts() {
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword("wrong-password");

        when(loginRateLimitService.tryConsume("127.0.0.1")).thenReturn(true);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid email or password");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getFailedLoginAttempts()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should lock account after 5 failed attempts")
    void shouldLockAccountAfterMaxAttempts() {
        LoginRequest request = new LoginRequest();
        request.setEmail(testEmail);
        request.setPassword("wrong-password");

        testUser.setFailedLoginAttempts(4);

        when(loginRateLimitService.tryConsume("127.0.0.1")).thenReturn(true);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad credentials"));

        assertThatThrownBy(() -> authService.login(request, "127.0.0.1"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid email or password");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());
        User saved = userCaptor.getValue();
        assertThat(saved.getFailedLoginAttempts()).isEqualTo(5);
        assertThat(saved.getLockedUntil()).isNotNull();
    }

    // === Refresh Token ===

    @Test
    @DisplayName("Should refresh access token successfully")
    void shouldRefreshTokenSuccessfully() {
        String rawToken = "raw-refresh-token";
        RefreshToken stored = RefreshToken.builder()
                .id(1L)
                .userId(testUserId)
                .tokenHash("hashed")
                .isRevoked(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(stored));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(jwtUtil.generateToken(any(CustomUserDetails.class), eq(testUserId), eq(UserRole.CUSTOMER.name())))
                .thenReturn("new-access-token");
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

        AuthResponse response = authService.refreshAccessToken(rawToken);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("new-access-token");
        assertThat(stored.getIsRevoked()).isTrue();
        verify(refreshTokenRepository, times(2)).save(any());
    }

    @Test
    @DisplayName("Should throw when refresh token is invalid")
    void shouldThrowOnInvalidRefreshToken() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refreshAccessToken("bad-token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid refresh token");
    }

    @Test
    @DisplayName("Should throw when refresh token is revoked")
    void shouldThrowOnRevokedRefreshToken() {
        RefreshToken stored = RefreshToken.builder()
                .id(1L)
                .userId(testUserId)
                .tokenHash("hashed")
                .isRevoked(true)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> authService.refreshAccessToken("revoked-token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Refresh token expired or revoked");
    }

    // === Logout ===

    @Test
    @DisplayName("Should revoke refresh token on logout")
    void shouldRevokeTokenOnLogout() {
        RefreshToken stored = RefreshToken.builder()
                .id(1L)
                .userId(testUserId)
                .tokenHash("hashed")
                .isRevoked(false)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(stored));

        authService.logout(testUserId, "raw-token");

        assertThat(stored.getIsRevoked()).isTrue();
        verify(refreshTokenRepository).save(stored);
    }

    @Test
    @DisplayName("Should not throw when logging out with null token")
    void shouldNotThrowOnLogoutWithNullToken() {
        assertThatNoException().isThrownBy(() -> authService.logout(testUserId, null));
        assertThatNoException().isThrownBy(() -> authService.logout(testUserId, ""));
    }

    // === Forgot Password ===

    @Test
    @DisplayName("Should generate password reset token for valid email")
    void shouldGenerateResetToken() {
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(passwordResetTokenRepository.save(any(PasswordResetToken.class)))
                .thenAnswer(i -> i.getArgument(0));

        String token = authService.forgotPassword(testEmail);

        assertThat(token).isNotNull();
        assertThat(token).isNotEmpty();
        verify(passwordResetTokenRepository).deleteByUserId(testUserId);
        verify(emailService).sendPasswordResetEmail(eq(testEmail), eq(testName), any());
    }

    @Test
    @DisplayName("Should return null for unknown email in forgot password")
    void shouldReturnNullForUnknownEmail() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        String result = authService.forgotPassword("unknown@example.com");

        assertThat(result).isNull();
        verifyNoInteractions(emailService);
    }

    // === Reset Password ===

    @Test
    @DisplayName("Should reset password with valid token")
    void shouldResetPassword() {
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .id(1L)
                .token("valid-token")
                .userId(testUserId)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        when(passwordResetTokenRepository.findByTokenAndUsedFalseAndExpiresAtAfter(
                eq("valid-token"), any(LocalDateTime.class)))
                .thenReturn(Optional.of(resetToken));
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode("New@12345")).thenReturn("new-encoded");

        authService.resetPassword("valid-token", "New@12345");

        verify(userRepository).save(testUser);
        assertThat(testUser.getPasswordHash()).isEqualTo("new-encoded");
        assertThat(resetToken.getUsed()).isTrue();
        verify(refreshTokenRepository).revokeAllByUserId(testUserId);
    }

    @Test
    @DisplayName("Should throw when reset token is invalid")
    void shouldThrowOnInvalidResetToken() {
        when(passwordResetTokenRepository.findByTokenAndUsedFalseAndExpiresAtAfter(
                eq("bad-token"), any(LocalDateTime.class)))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.resetPassword("bad-token", "New@12345"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid or expired reset token");
    }

    @Test
    @DisplayName("Should throw when new password is too short")
    void shouldThrowOnShortNewPassword() {
        assertThatThrownBy(() -> authService.resetPassword("token", "Ab1@"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("at least 8 characters");
    }

    // === Get Current User ===

    @Test
    @DisplayName("Should return current authenticated user")
    void shouldGetCurrentUser() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        AuthResponse response = authService.getCurrentUser();

        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo(testEmail);
        assertThat(response.getUserId()).isEqualTo(testUserId);
    }

    @Test
    @DisplayName("Should throw when getting current user with no auth")
    void shouldThrowOnGetCurrentUserNoAuth() {
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(null);
        SecurityContextHolder.setContext(securityContext);

        assertThatThrownBy(() -> authService.getCurrentUser())
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("No authenticated user found");
    }

    // === Update Profile ===

    @Test
    @DisplayName("Should update user profile")
    void shouldUpdateProfile() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));

        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setName("New Name");
        request.setPhone("+1987654321");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        AuthResponse response = authService.updateProfile(request);

        assertThat(response).isNotNull();
        verify(userRepository).save(any(User.class));
    }

    // === Change Password ===

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePassword() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPass", testUser.getPasswordHash())).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("new-encoded-hash");

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("oldPass");
        request.setNewPassword("newPass");

        authService.changePassword(request);

        assertThat(testUser.getPasswordHash()).isEqualTo("new-encoded-hash");
        verify(refreshTokenRepository).revokeAllByUserId(testUserId);
    }

    @Test
    @DisplayName("Should throw when current password is wrong")
    void shouldThrowOnWrongCurrentPassword() {
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", testUser.getPasswordHash())).thenReturn(false);

        ChangePasswordRequest request = new ChangePasswordRequest();
        request.setCurrentPassword("wrong");
        request.setNewPassword("newPass");

        assertThatThrownBy(() -> authService.changePassword(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Current password is incorrect");
    }
}
