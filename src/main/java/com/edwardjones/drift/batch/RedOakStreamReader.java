package com.edwardjones.drift.batch;

import com.edwardjones.drift.infra.TokenService;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    private JsonParser parser;
    private boolean isOpen = false;

    @Override
    public void open(ExecutionContext executionContext) {
        try {
            log.info("Opening RedOakStreamReader for URL: {}", url);

            // Create headers with authentication token
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + tokenService.fetchToken());
            headers.set("Accept", "application/json");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            // Fix: Stream directly from HTTP response to avoid loading entire JSON into memory
            parser = restTemplate.execute(url, HttpMethod.GET,
                request -> {
                    request.getHeaders().putAll(headers);
                },
                response -> {
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new IllegalStateException(
                            String.format("HTTP request failed with status %s for URL: %s. Headers: %s",
                                response.getStatusCode(), url, response.getHeaders()));
                    }
                    // Create parser directly from response stream
                    return objectMapper.getFactory().createParser(response.getBody());
                });

            if (parser == null) {
                throw new IllegalStateException("Failed to create JSON parser for URL: " + url);
            }

            // Position parser at START_ARRAY
            JsonToken token = parser.nextToken();

            if (token != JsonToken.START_ARRAY) {
                throw new IllegalStateException(
                    String.format("Expected JSON array, but got %s from URL: %s", token, url));
            }

            isOpen = true;
            log.debug("Successfully opened RedOakStreamReader for {}", url);

        } catch (IOException e) {
            throw new IllegalStateException("Failed to parse JSON response from URL: " + url, e);
        } catch (Exception e) {
            throw new IllegalStateException(
                String.format("Failed to open RedOakStreamReader for URL: %s. Error: %s",
                    url, e.getMessage()), e);
        }
    }

    @Override
    public T read() {
        if (!isOpen) {
            throw new IllegalStateException("RedOakStreamReader is not open");
        }

        try {
            JsonToken token = parser.nextToken();

            if (token == JsonToken.END_ARRAY) {
                // End of array reached
                return null;
            }

            if (token == JsonToken.START_OBJECT) {
                // Parse the current object
                return objectMapper.readValue(parser, targetType);
            }

            throw new IllegalStateException(
                String.format("Unexpected token %s while reading from URL: %s", token, url));

        } catch (IOException e) {
            throw new IllegalStateException(
                String.format("Failed to read JSON object from URL: %s", url), e);
        }
    }

    @Override
    public void close() {
        if (parser != null) {
            try {
                parser.close();
                log.debug("Closed RedOakStreamReader for {}", url);
            } catch (IOException e) {
                log.warn("Error closing JSON parser for URL: {}", url, e);
            }
        }
        isOpen = false;
    }

    @Override
    public void update(ExecutionContext executionContext) {
        // No-op - this is a stateless reader that doesn't need to persist state
        // The reader processes the entire JSON array in a single batch job execution
    }
}
