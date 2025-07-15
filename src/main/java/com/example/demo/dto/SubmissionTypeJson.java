package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SubmissionTypeJson(
        long id,
        String name,
        boolean active,
        String initialState,
        int priority,
        List<String> approvedStates,
        List<String> formats,
        String description,
        String helpText) {}
