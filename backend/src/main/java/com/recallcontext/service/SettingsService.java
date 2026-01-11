package com.recallcontext.service;

import com.recallcontext.exception.ApiKeyNotFoundException;
import com.recallcontext.model.entity.UserSettings;
import com.recallcontext.repository.UserSettingsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SettingsService {

    private final UserSettingsRepository userSettingsRepository;
    private final EncryptionService encryptionService;

    private static final String DEFAULT_USER_ID = "default-user";

    /**
     * Store or update the user's API key (encrypted)
     */
    @Transactional
    public void saveApiKey(String apiKey) {
        log.info("Saving API key for user: {}", DEFAULT_USER_ID);

        // Encrypt the API key
        EncryptionService.EncryptionResult encryptionResult =
                encryptionService.encrypt(apiKey, DEFAULT_USER_ID);

        // Find or create user settings
        UserSettings settings = userSettingsRepository
                .findByUserId(DEFAULT_USER_ID)
                .orElse(UserSettings.builder()
                        .userId(DEFAULT_USER_ID)
                        .build());

        settings.setEncryptedApiKey(encryptionResult.getEncryptedApiKey());
        settings.setEncryptionIv(encryptionResult.getIv());

        userSettingsRepository.save(settings);
        log.info("API key saved successfully for user: {}", DEFAULT_USER_ID);
    }

    /**
     * Retrieve the decrypted API key
     */
    public String getApiKey() {
        log.debug("Retrieving API key for user: {}", DEFAULT_USER_ID);

        UserSettings settings = userSettingsRepository
                .findByUserId(DEFAULT_USER_ID)
                .orElseThrow(() -> new ApiKeyNotFoundException("API key not configured. Please configure your Anthropic API key in settings."));

        // Check if API key is configured (not the placeholder)
        if ("not-configured".equals(settings.getEncryptedApiKey())) {
            throw new ApiKeyNotFoundException("API key not configured. Please configure your Anthropic API key in settings.");
        }

        // Decrypt and return
        return encryptionService.decrypt(
                settings.getEncryptedApiKey(),
                settings.getEncryptionIv(),
                DEFAULT_USER_ID
        );
    }

    /**
     * Check if API key is configured
     */
    public boolean isApiKeyConfigured() {
        return userSettingsRepository
                .findByUserId(DEFAULT_USER_ID)
                .map(settings -> !"not-configured".equals(settings.getEncryptedApiKey()))
                .orElse(false);
    }

    /**
     * Delete the API key
     */
    @Transactional
    public void deleteApiKey() {
        log.info("Deleting API key for user: {}", DEFAULT_USER_ID);

        UserSettings settings = userSettingsRepository
                .findByUserId(DEFAULT_USER_ID)
                .orElseThrow(() -> new ApiKeyNotFoundException("API key not found"));

        settings.setEncryptedApiKey("not-configured");
        settings.setEncryptionIv("not-configured");

        userSettingsRepository.save(settings);
        log.info("API key deleted successfully for user: {}", DEFAULT_USER_ID);
    }
}
