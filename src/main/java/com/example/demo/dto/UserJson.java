package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record UserJson(
        String userName,
        String firstName,
        String lastName,
        String emailAddress,
        boolean active,
        String timeZone,
        String locale,
        String visibilityProfile,
        List<GroupRef> groups) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GroupRef(String groupName, boolean active) {}
}
