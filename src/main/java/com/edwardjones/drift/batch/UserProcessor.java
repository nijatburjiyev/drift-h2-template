package com.edwardjones.drift.batch;

import com.edwardjones.drift.domain.*;
import com.edwardjones.drift.dto.UserJson;
import com.edwardjones.drift.repo.VisibilityProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class UserProcessor implements ItemProcessor<UserJson, User> {

    private final VisibilityProfileRepository vpRepo;

    @Override
    public User process(UserJson json) {
        User user = new User();
        user.setUserName(json.userName());
        user.setFirstName(json.firstName());
        user.setLastName(json.lastName());
        user.setEmailAddress(json.emailAddress());
        user.setActive(json.active());
        user.setTimeZone(json.timeZone());
        user.setLocale(json.locale());
        user.setLandingPage(json.landingPage());
        user.setRestrictByIpAddress(json.restrictByIpAddress());
        user.setSsoOnly(json.ssoOnly());

        // Handle roles - convert List to Set
        if (json.roles() != null) {
            user.setRoles(new HashSet<>(json.roles()));
        }

        // Handle admin roles - convert List to Set
        if (json.adminRoles() != null) {
            user.setAdminRoles(new HashSet<>(json.adminRoles()));
        }

        // Handle admin role permissions - flatten List<List<String>> to Set<String>
        if (json.adminRolePermissions() != null) {
            Set<String> permissions = new HashSet<>();
            for (List<String> permissionPair : json.adminRolePermissions()) {
                if (permissionPair != null && permissionPair.size() >= 2) {
                    // Combine role and permission as "ROLE:PERMISSION"
                    permissions.add(permissionPair.get(0) + ":" + permissionPair.get(1));
                }
            }
            user.setAdminRolePermissions(permissions);
        }

        // Link to visibility profile if it already exists
        if (json.visibilityProfile() != null) {
            vpRepo.findById(json.visibilityProfile()).ifPresent(user::setVisibilityProfile);
        }

        // Don't handle group relationships here - let the groups manage the relationships
        // This avoids potential circular dependency issues

        return user;
    }
}
