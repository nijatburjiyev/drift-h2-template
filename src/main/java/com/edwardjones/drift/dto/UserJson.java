package com.edwardjones.drift.dto;

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
        List<String> roles,
        List<String> adminRoles,
        List<GroupRef> groups,
        String landingPage,
        boolean restrictByIpAddress,
        boolean ssoOnly,
        List<List<String>> adminRolePermissions) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GroupRef(String groupName, boolean active) {}
}
