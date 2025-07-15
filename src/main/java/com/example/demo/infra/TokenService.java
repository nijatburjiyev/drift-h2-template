package com.example.demo.infra;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TokenService {
    private final RestTemplate rest;

    public String fetchToken() {
        // Simple one-shot call – adapt to vendor token endpoint
        record Token(String access_token) {}
        return rest.getForObject(
            "https://api.redoak.example.com/getToken",
            Token.class
        ).access_token();
    }
}
