package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VisibilityProfileJson(
        String name,
        boolean canOnlyViewInvolvedSubmissions,
        boolean canViewAllSubmitterGroups,
        boolean canViewAllSubmissionTypes,
        boolean canSubmitOnBehalfOfSelf,
        List<GroupJson> onBehalfOfGroups,
        List<GroupJson> canViewSubmissionsForGroups,
        List<SubmissionTypeJson> canViewSubmissionTypes,
        boolean active) {}
