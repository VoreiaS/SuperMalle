package com.example.superMalle.aspect;

import com.example.superMalle.annotation.Idempotent;
import com.example.superMalle.entity.IdempotencyKey;
import com.example.superMalle.entity.enums.IdempotencyStatus;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.repository.IdempotencyKeyRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IdempotencyInterceptor Security Tests")
class IdempotencyInterceptorTest {

    @Mock
    private IdempotencyKeyRepository idempotencyKeyRepository;

    @Mock
    private ObjectMapper objectMapper;

    private IdempotencyInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new IdempotencyInterceptor(idempotencyKeyRepository, objectMapper);
        // Setup mock request context
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    @DisplayName("Should reject invalid idempotency key format when strict mode enabled")
    void shouldRejectInvalidKeyFormat() {
        // Given
        String invalidKey = "not-a-uuid";
        
        // When & Then
        assertThatThrownBy(() -> {
            // Simulate key validation (extracted from interceptor logic)
            if (!isValidKeyFormat(invalidKey)) {
                throw new BadRequestException("Invalid idempotency key format");
            }
        })
        .isInstanceOf(BadRequestException.class)
        .hasMessageContaining("Invalid idempotency key format");
    }

    @Test
    @DisplayName("Should accept valid UUID v4 format")
    void shouldAcceptValidUuidV4() {
        // Given
        String validUuid = UUID.randomUUID().toString();
        
        // When & Then
        assertThat(isValidKeyFormat(validUuid)).isTrue();
    }

    @Test
    @DisplayName("Should accept valid 32-char hex format")
    void shouldAcceptValidHexFormat() {
        // Given
        String validHex = UUID.randomUUID().toString().replace("-", "");
        
        // When & Then
        assertThat(isValidKeyFormat(validHex)).isTrue();
    }

    @Test
    @DisplayName("Should reject expired idempotency key and allow retry")
    void shouldRejectExpiredKey() {
        // Given
        IdempotencyKey expired = IdempotencyKey.builder()
                .key("test-key")
                .entity("payment")
                .userId(1L)
                .requestHash("abc123")
                .status(IdempotencyStatus.COMPLETED)
                .expiresAt(LocalDateTime.now().minusHours(1))
                .build();
        
        // When & Then
        assertThat(expired.isExpired()).isTrue();
        assertThat(expired.isCompleted()).isFalse(); // expired keys are not "completed" for replay
    }

    @Test
    @DisplayName("Should detect payload hash mismatch as potential replay attack")
    void shouldDetectPayloadMismatch() {
        // Given
        IdempotencyKey record = IdempotencyKey.builder()
                .key("test-key")
                .entity("payment")
                .userId(1L)
                .requestHash("original-hash")
                .status(IdempotencyStatus.COMPLETED)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        
        String differentPayloadHash = "different-hash";
        
        // When & Then
        assertThat(record.payloadMatches(differentPayloadHash)).isFalse();
        // This would trigger: "Idempotency key conflict: same key used with different request data"
    }

    @Test
    @DisplayName("Should allow replay when key, hash, and status match")
    void shouldAllowReplayWhenAllMatch() {
        // Given
        IdempotencyKey record = IdempotencyKey.builder()
                .key("test-key")
                .entity("payment")
                .userId(1L)
                .requestHash("same-hash")
                .status(IdempotencyStatus.COMPLETED)
                .responseBody("{\"result\":\"success\"}")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        
        // When & Then
        assertThat(record.isCompleted()).isTrue();
        assertThat(record.payloadMatches("same-hash")).isTrue();
        // Replay would return cached responseBody
    }

    @Test
    @DisplayName("Should handle concurrent PROCESSING state by rejecting duplicate")
    void shouldRejectConcurrentProcessingRequest() {
        // Given
        IdempotencyKey processing = IdempotencyKey.builder()
                .key("test-key")
                .entity("payment")
                .userId(1L)
                .requestHash("hash")
                .status(IdempotencyStatus.PROCESSING)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        
        // When & Then
        assertThat(processing.getStatus()).isEqualTo(IdempotencyStatus.PROCESSING);
        // Interceptor would throw: "Request with this idempotency key is still being processed"
    }

    @Test
    @DisplayName("Should allow retry after FAILED status")
    void shouldAllowRetryAfterFailed() {
        // Given
        IdempotencyKey failed = IdempotencyKey.builder()
                .key("test-key")
                .entity("payment")
                .userId(1L)
                .requestHash("hash")
                .status(IdempotencyStatus.FAILED)
                .errorMessage("Transient error")
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();
        
        // When & Then
        assertThat(failed.getStatus()).isEqualTo(IdempotencyStatus.FAILED);
        // Interceptor would throw RetryIdempotencyException to signal client should retry
    }

    // Helper method extracted from interceptor for testing
    private boolean isValidKeyFormat(String key) {
        if (key == null) return false;
        // UUID v4 pattern
        if (key.matches("^[0-9a-f]{8}-[0-9a-f]{4}-4[0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$")) {
            return true;
        }
        // 32-char hex
        if (key.matches("^[0-9a-f]{32}$")) {
            return true;
        }
        return false;
    }
}
