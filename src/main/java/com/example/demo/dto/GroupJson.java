package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record GroupJson(
        String groupName,
        String groupDescription,
        boolean active,
        List<UserRef> users) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UserRef(String userName, String emailAddress, String name, boolean active) {}
}
