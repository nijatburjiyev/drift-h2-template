package com.edwardjones.drift.batch;

import com.edwardjones.drift.infra.TokenService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class RedOakStreamReader<T> implements ItemStreamReader<T> {

    private final Class<T> targetType;
    private final String url;
    private final RestTemplate restTemplate;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    private JsonParser parser;
    private boolean isOpen = false;
    private int recordCount = 0;
    private int resumeFromIndex = 0;

    // Configuration flags
    private static final boolean SKIP_MALFORMED_RECORDS = true; // Set to false to fail on malformed records
    private static final int LARGE_PAYLOAD_WARNING_MB = 50;
    private static final String EXECUTION_CONTEXT_KEY = "redoak.reader.recordCount";

    @Override
    public void open(ExecutionContext executionContext) {
        try {
            log.info("Opening RedOakStreamReader for URL: {}", url);
            log.debug("Target type: {}", targetType.getSimpleName());

            // Check for restart scenario
            if (executionContext != null && executionContext.containsKey(EXECUTION_CONTEXT_KEY)) {
                resumeFromIndex = executionContext.getInt(EXECUTION_CONTEXT_KEY);
                log.info("Resuming from record index: {} for URL: {}", resumeFromIndex, url);
            }

            // Create headers with authentication token
            HttpHeaders headers = new HttpHeaders();
            String token = tokenService.fetchToken();
            headers.set("Authorization", "Bearer " + token);
            headers.set("Accept", "application/json");

            log.debug("Request headers prepared - Authorization: Bearer {}...",
                token.substring(0, Math.min(20, token.length())));

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Get the response as a string first to avoid stream closure issues
            log.debug("Making HTTP GET request to: {}", url);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            log.info("HTTP response received - Status: {}, Content-Type: {}",
                response.getStatusCode(), response.getHeaders().getContentType());

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("HTTP request failed - Status: {}, Headers: {}",
                    response.getStatusCode(), response.getHeaders());
                throw new IllegalStateException(
                    String.format("HTTP request failed with status %s for URL: %s. Headers: %s",
                        response.getStatusCode(), url, response.getHeaders()));
            }

            String responseBody = response.getBody();
            if (responseBody == null || responseBody.isEmpty()) {
                log.error("Empty response body received from URL: {}", url);
                throw new IllegalStateException("Empty response body from URL: " + url);
            }

            log.debug("Response body received - Length: {} characters, Preview: {}...",
                responseBody.length(),
                responseBody.substring(0, Math.min(100, responseBody.length())));

            // Memory usage warning for large payloads
            long responseSizeMB = responseBody.length() / (1024 * 1024);
            if (responseSizeMB > LARGE_PAYLOAD_WARNING_MB) {
                log.warn("Large response body detected ({} MB) - consider streaming implementation for better memory usage", responseSizeMB);
            }

            // Create parser from the response string
            log.debug("Creating JSON parser from response body");
            parser = objectMapper.getFactory().createParser(responseBody);

            if (parser == null) {
                log.error("Failed to create JSON parser for URL: {}", url);
                throw new IllegalStateException("Failed to create JSON parser for URL: " + url);
            }

            // Position parser at START_ARRAY
            log.debug("Positioning parser at START_ARRAY");
            JsonToken jsonToken = parser.nextToken();

            if (jsonToken != JsonToken.START_ARRAY) {
                log.error("Expected JSON array but got token: {} from URL: {}", jsonToken, url);
                throw new IllegalStateException(
                    String.format("Expected JSON array, but got %s from URL: %s", jsonToken, url));
            }

            // Skip records if resuming from a restart
            if (resumeFromIndex > 0) {
                log.info("Skipping {} records to resume from index {}", resumeFromIndex, resumeFromIndex);
                skipToResumePosition();
            }

            isOpen = true;
            recordCount = resumeFromIndex;
            log.info("Successfully opened RedOakStreamReader for {} - Ready to read {} objects{}",
                url, targetType.getSimpleName(), resumeFromIndex > 0 ? " (resumed from index " + resumeFromIndex + ")" : "");

        } catch (IOException e) {
            log.error("Failed to parse JSON response from URL: {} - Error: {}", url, e.getMessage(), e);
            throw new IllegalStateException("Failed to parse JSON response from URL: " + url, e);
        } catch (Exception e) {
            log.error("Failed to open RedOakStreamReader for URL: {} - Error: {}", url, e.getMessage(), e);
            throw new IllegalStateException(
                String.format("Failed to open RedOakStreamReader for URL: %s. Error: %s",
                    url, e.getMessage()), e);
        }
    }

    private void skipToResumePosition() throws IOException {
        for (int i = 0; i < resumeFromIndex; i++) {
            JsonToken jsonToken = parser.nextToken();
            if (jsonToken == JsonToken.END_ARRAY) {
                log.warn("Reached end of array while skipping to resume position {} - only {} records available", resumeFromIndex, i);
                return;
            }
            if (jsonToken == JsonToken.START_OBJECT) {
                parser.skipChildren(); // Skip the entire object
            }
        }
    }

    @Override
    public T read() {
        if (!isOpen) {
            log.error("Attempted to read from closed RedOakStreamReader for URL: {}", url);
            throw new IllegalStateException("RedOakStreamReader is not open");
        }

        try {
            log.trace("Reading next token from JSON stream");
            JsonToken jsonToken = parser.nextToken();

            if (jsonToken == JsonToken.END_ARRAY) {
                log.info("End of JSON array reached - Processed {} records from URL: {}", recordCount, url);
                return null;
            }

            if (jsonToken == JsonToken.START_OBJECT) {
                log.trace("Parsing {} object from JSON stream", targetType.getSimpleName());

                try {
                    T result = objectMapper.readValue(parser, targetType);
                    recordCount++;
                    log.trace("Successfully parsed {} object (record #{})", targetType.getSimpleName(), recordCount);
                    return result;
                } catch (Exception e) {
                    recordCount++; // Increment even for failed records to maintain position tracking

                    // Enhanced error handling with JSON context
                    log.error("Failed to parse {} object at record #{} from URL: {} - Error: {}",
                        targetType.getSimpleName(), recordCount, url, e.getMessage());

                    // Try to get parser location context
                    try {
                        String currentLocation = parser.getCurrentLocation().toString();
                        log.error("JSON parser location: {}", currentLocation);
                    } catch (Exception contextEx) {
                        log.debug("Could not capture JSON context", contextEx);
                    }

                    if (SKIP_MALFORMED_RECORDS) {
                        log.warn("Skipping malformed record #{} and continuing", recordCount);
                        // Skip this record and continue - return null to indicate skip
                        return null;
                    } else {
                        // Re-throw to maintain existing error handling behavior
                        throw e;
                    }
                }
            }

            log.error("Unexpected JSON token while reading from URL: {} at record #{} - Expected START_OBJECT or END_ARRAY, got: {}",
                url, recordCount + 1, jsonToken);
            throw new IllegalStateException(
                String.format("Unexpected token %s while reading from URL: %s at record #%d",
                    jsonToken, url, recordCount + 1));

        } catch (IOException e) {
            log.error("Failed to read JSON object from URL: {} at record #{} - Error: {}",
                url, recordCount + 1, e.getMessage(), e);
            throw new IllegalStateException(
                String.format("Failed to read JSON object from URL: %s at record #%d", url, recordCount + 1), e);
        }
    }

    @Override
    public void close() {
        log.debug("Closing RedOakStreamReader for URL: {} - Processed {} records", url, recordCount);
        if (parser != null) {
            try {
                parser.close();
                log.info("Successfully closed RedOakStreamReader for {} - Final record count: {}", url, recordCount);
            } catch (IOException e) {
                log.warn("Error closing JSON parser for URL: {} - Error: {}", url, e.getMessage(), e);
            }
        }
        isOpen = false;
    }

    @Override
    public void update(ExecutionContext executionContext) {
        // Store the current record count for restart scenarios
        if (executionContext != null) {
            executionContext.putInt(EXECUTION_CONTEXT_KEY, recordCount);
            log.trace("Update called on RedOakStreamReader for URL: {} - Stored record count: {}", url, recordCount);
        }
    }
}
