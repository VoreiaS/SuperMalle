package com.example.superMalle.security;

import com.example.superMalle.config.OAuth2Config;
import com.example.superMalle.entity.User;
import com.example.superMalle.entity.enums.UserRole;
import com.example.superMalle.repository.UserRepository;
import com.example.superMalle.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Conditional;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@Conditional(OAuth2Config.GoogleCredentialsPresent.class)
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Override
    @Transactional
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        if (!(authentication instanceof OAuth2AuthenticationToken oauthToken)) {
            response.sendRedirect("/");
            return;
        }

        OAuth2User oauthUser = oauthToken.getPrincipal();
        String email = oauthUser.getAttribute("email");
        String name = oauthUser.getAttribute("name");

        if (email == null || email.isBlank()) {
            log.warn("OAuth2 login without email attribute");
            response.sendRedirect("/login?error=no_email");
            return;
        }

        // Find or create user
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            log.info("Creating new user from OAuth2: {}", email);
            User newUser = User.builder()
                    .name(name != null ? name : email.split("@")[0])
                    .email(email)
                    .passwordHash("") // No password for OAuth users
                    .role(UserRole.CUSTOMER)
                    .isActive(true)
                    .emailVerified(true) // OAuth emails are verified
                    .build();
            return userRepository.save(newUser);
        });

        if (!user.getIsActive()) {
            response.sendRedirect("/login?error=deactivated");
            return;
        }

        // Generate JWT
        CustomUserDetails userDetails = new CustomUserDetails(user);
        String token = jwtUtil.generateToken(userDetails, user.getId(), user.getRole().name());

        // Redirect to frontend with token
        String frontendUrl = allowedOrigins.split(",")[0].trim();
        String redirectUrl = frontendUrl + "/oauth2/callback?token=" + token +
                "&userId=" + user.getId() +
                "&name=" + java.net.URLEncoder.encode(user.getName(), java.nio.charset.StandardCharsets.UTF_8) +
                "&email=" + java.net.URLEncoder.encode(user.getEmail(), java.nio.charset.StandardCharsets.UTF_8) +
                "&role=" + user.getRole().name();

        log.info("OAuth2 login success for user: {}", email);
        response.sendRedirect(redirectUrl);
    }
}
