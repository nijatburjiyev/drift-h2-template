package com.edwardjones.drift.infra;

import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    private final RestTemplate vendorRestTemplate;

    // Token cache with expiration
    private volatile String cachedToken;
    private volatile Instant tokenExpiry;
    private final Object tokenLock = new Object();

    public String fetchToken() {
        // Check if cached token is still valid
        if (cachedToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry)) {
            log.debug("Using cached token (expires at: {})", tokenExpiry);
            return cachedToken;
        }

        synchronized (tokenLock) {
            // Double-check pattern - another thread might have refreshed the token
            if (cachedToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry)) {
                return cachedToken;
            }

            log.debug("Fetching new token from vendor API");

            // Retry logic for token fetching
            int maxRetries = 3;
            for (int attempt = 1; attempt <= maxRetries; attempt++) {
                try {
                    record TokenResponse(String access_token, Long expires_in) {}

                    var tokenResponse = vendorRestTemplate.getForObject(
                        "https://api.redoak.example.com/getToken", TokenResponse.class);

                    if (tokenResponse != null && tokenResponse.access_token() != null) {
                        cachedToken = tokenResponse.access_token();
                        // Default to 1 hour if no expires_in provided, otherwise use 90% of actual expiry
                        long expirySeconds = tokenResponse.expires_in() != null ?
                            (long) (tokenResponse.expires_in() * 0.9) : 3600;
                        tokenExpiry = Instant.now().plus(expirySeconds, ChronoUnit.SECONDS);

                        log.debug("Token cached successfully (expires at: {})", tokenExpiry);
                        return cachedToken;
                    }

                    log.warn("Received null or empty token response on attempt {}", attempt);
                } catch (Exception e) {
                    log.warn("Failed to fetch token on attempt {} of {}: {}", attempt, maxRetries, e.getMessage());
                    if (attempt == maxRetries) {
                        throw new IllegalStateException("Failed to fetch access token after " + maxRetries + " attempts", e);
                    }

                    // Brief pause before retry
                    try {
                        Thread.sleep(1000 * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException("Token fetch interrupted", ie);
                    }
                }
            }

            throw new IllegalStateException("Failed to fetch access token from vendor API after " + maxRetries + " attempts");
        }
    }

    // Method to clear cache if needed (for testing or error recovery)
    public void clearTokenCache() {
        synchronized (tokenLock) {
            cachedToken = null;
            tokenExpiry = null;
            log.debug("Token cache cleared");
        }
    }
}
