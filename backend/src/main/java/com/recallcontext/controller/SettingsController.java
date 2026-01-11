package com.recallcontext.controller;

import com.recallcontext.model.dto.ApiKeyRequest;
import com.recallcontext.model.dto.ApiKeyStatusResponse;
import com.recallcontext.service.SettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/settings")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SettingsController {

    private final SettingsService settingsService;

    /**
     * Store or update API key
     */
    @PostMapping("/api-key")
    public ResponseEntity<ApiKeyStatusResponse> saveApiKey(@Valid @RequestBody ApiKeyRequest request) {
        log.info("Received request to save API key");
        settingsService.saveApiKey(request.getApiKey());

        return ResponseEntity.ok(ApiKeyStatusResponse.builder()
                .configured(true)
                .message("API key saved successfully")
                .build());
    }

    /**
     * Check API key status
     */
    @GetMapping("/api-key/status")
    public ResponseEntity<ApiKeyStatusResponse> getApiKeyStatus() {
        boolean configured = settingsService.isApiKeyConfigured();

        return ResponseEntity.ok(ApiKeyStatusResponse.builder()
                .configured(configured)
                .message(configured ? "API key is configured" : "API key not configured")
                .build());
    }

    /**
     * Delete API key
     */
    @DeleteMapping("/api-key")
    public ResponseEntity<ApiKeyStatusResponse> deleteApiKey() {
        log.info("Received request to delete API key");
        settingsService.deleteApiKey();

        return ResponseEntity.ok(ApiKeyStatusResponse.builder()
                .configured(false)
                .message("API key deleted successfully")
                .build());
    }
}
