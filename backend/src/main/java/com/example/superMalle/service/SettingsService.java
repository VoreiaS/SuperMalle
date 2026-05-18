package com.example.superMalle.service;

import com.example.superMalle.dto.admin.SettingsRequest;
import com.example.superMalle.dto.admin.SettingsResponse;
import com.example.superMalle.entity.Settings;
import com.example.superMalle.exception.BadRequestException;
import com.example.superMalle.exception.ResourceNotFoundException;
import com.example.superMalle.repository.SettingsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SettingsService {

    private final SettingsRepository settingsRepository;

    public List<SettingsResponse> getAllSettings() {
        return settingsRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public SettingsResponse getSettingByKey(String key) {
        if (key == null || key.isBlank()) {
            throw new BadRequestException("Setting key is required");
        }
        Settings setting = settingsRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Settings", "key", key));
        return toResponse(setting);
    }

    @Transactional
    public SettingsResponse upsertSetting(SettingsRequest request) {
        if (request == null) {
            throw new BadRequestException("Settings request cannot be null");
        }
        if (request.getKey() == null || request.getKey().isBlank()) {
            throw new BadRequestException("Setting key is required");
        }
        if (request.getValue() == null) {
            throw new BadRequestException("Setting value is required");
        }
        Settings setting = settingsRepository.findByKey(request.getKey())
                .orElseGet(() -> Settings.builder().key(request.getKey()).build());
        setting.setValue(request.getValue());
        if (request.getDescription() != null) {
            setting.setDescription(request.getDescription());
        }
        return toResponse(settingsRepository.save(setting));
    }

    @Transactional
    public void deleteSetting(String key) {
        if (key == null || key.isBlank()) {
            throw new BadRequestException("Setting key is required");
        }
        Settings setting = settingsRepository.findByKey(key)
                .orElseThrow(() -> new ResourceNotFoundException("Settings", "key", key));
        settingsRepository.delete(setting);
    }

    private SettingsResponse toResponse(Settings setting) {
        return SettingsResponse.builder()
                .id(setting.getId())
                .key(setting.getKey())
                .value(setting.getValue())
                .description(setting.getDescription())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}
