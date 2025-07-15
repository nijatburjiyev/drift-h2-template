package com.edwardjones.drift.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Builder;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
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
