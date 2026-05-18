package com.example.superMalle.controller.admin;

import com.example.superMalle.dto.admin.AdminResetPasswordRequest;
import com.example.superMalle.dto.admin.AdminUserRequest;
import com.example.superMalle.dto.admin.AdminUserResponse;
import com.example.superMalle.dto.menu.PagedResponse;
import com.example.superMalle.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<PagedResponse<AdminUserResponse>> getAllUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminUserService.getAllUsers(search, page, size));
    }

    @GetMapping("/{id:\\d+}")
    public ResponseEntity<AdminUserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.getUserById(id));
    }

    @PostMapping
    public ResponseEntity<AdminUserResponse> createUser(@Valid @RequestBody AdminUserRequest request) {
        return ResponseEntity.ok(adminUserService.createUser(request));
    }

    @PutMapping("/{id:\\d+}")
    public ResponseEntity<AdminUserResponse> updateUser(@PathVariable Long id,
                                                        @Valid @RequestBody AdminUserRequest request) {
        return ResponseEntity.ok(adminUserService.updateUser(id, request));
    }

    @PatchMapping("/{id:\\d+}/toggle-active")
    public ResponseEntity<AdminUserResponse> toggleActive(@PathVariable Long id) {
        return ResponseEntity.ok(adminUserService.toggleActive(id));
    }

    @DeleteMapping("/{id:\\d+}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id:\\d+}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id,
                                               @Valid @RequestBody AdminResetPasswordRequest request) {
        adminUserService.resetPassword(id, request);
        return ResponseEntity.ok().build();
    }
}
