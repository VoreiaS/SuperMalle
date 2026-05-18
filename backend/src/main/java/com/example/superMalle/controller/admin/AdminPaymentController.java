package com.example.superMalle.controller.admin;

import com.example.superMalle.dto.menu.PagedResponse;
import com.example.superMalle.dto.payment.PaymentResponse;
import com.example.superMalle.dto.payment.RefundRequest;
import com.example.superMalle.dto.payment.RefundResponse;
import com.example.superMalle.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/payments")
@RequiredArgsConstructor
public class AdminPaymentController {

    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<PagedResponse<PaymentResponse>> getAllPayments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(paymentService.getPaymentHistory(page, size, status, from, to));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<PaymentResponse> getPaymentDetails(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderIdForAdmin(id));
    }

    @PostMapping("/{paymentId:\\d+}/refund")
    public ResponseEntity<RefundResponse> processRefund(@PathVariable Long paymentId,
                                                        @Valid @RequestBody RefundRequest request) {
        return ResponseEntity.ok(paymentService.processRefund(
                paymentId,
                request.getAmount(),
                request.getReason()
        ));
    }
}
