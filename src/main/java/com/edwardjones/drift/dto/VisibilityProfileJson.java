package com.edwardjones.drift.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VisibilityProfileJson(
        String name,
        boolean canOnlyViewInvolvedSubmissions,
        boolean canViewAllSubmitterGroups,
        boolean canViewAllSubmissionTypes,
        boolean canSubmitOnBehalfOfSelf,
        boolean active,
        List<GroupWithUsers> onBehalfOfGroups,
        List<GroupWithUsers> canViewSubmissionsForGroups,
        List<SubmissionTypeRef> canViewSubmissionTypes) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record GroupWithUsers(
            String groupName,
            boolean active,
            List<UserRef> users) {

        @JsonIgnoreProperties(ignoreUnknown = true)
        public record UserRef(String userName, String emailAddress, String name) {}
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SubmissionTypeRef(
            Long id,
            String name,
            boolean active,
            String initialState,
            int priority,
            List<String> approvedStates,
            List<String> formats,
            String description,
            String helpText) {}
}
