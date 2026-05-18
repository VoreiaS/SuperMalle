package com.example.superMalle.controller.admin;

import com.example.superMalle.dto.admin.SettingsRequest;
import com.example.superMalle.dto.admin.SettingsResponse;
import com.example.superMalle.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/settings")
@RequiredArgsConstructor
public class AdminSettingsController {

    private final SettingsService settingsService;

    @GetMapping
    public ResponseEntity<List<SettingsResponse>> getAllSettings() {
        return ResponseEntity.ok(settingsService.getAllSettings());
    }

    @GetMapping("/{key}")
    public ResponseEntity<SettingsResponse> getSettingByKey(@PathVariable String key) {
        return ResponseEntity.ok(settingsService.getSettingByKey(key));
    }

    @PutMapping
    public ResponseEntity<SettingsResponse> upsertSetting(@Valid @RequestBody SettingsRequest request) {
        return ResponseEntity.ok(settingsService.upsertSetting(request));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteSetting(@PathVariable String key) {
        settingsService.deleteSetting(key);
        return ResponseEntity.ok().build();
    }
}
