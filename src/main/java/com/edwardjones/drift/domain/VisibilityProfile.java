package com.edwardjones.drift.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.Set;

@Entity
@Table(name = "visibility_profiles")
@Data
@EqualsAndHashCode(exclude = {"users", "onBehalfOfGroups", "canViewSubmissionsForGroups", "canViewSubmissionTypes"})
@ToString(exclude = {"users", "onBehalfOfGroups", "canViewSubmissionsForGroups", "canViewSubmissionTypes"})
public class VisibilityProfile {
    @Id
    @Column(name = "name")
    private String name;

    @Column(name = "can_only_view_involved_submissions")
    private boolean canOnlyViewInvolvedSubmissions;

    @Column(name = "can_view_all_submitter_groups")
    private boolean canViewAllSubmitterGroups;

    @Column(name = "can_view_all_submission_types")
    private boolean canViewAllSubmissionTypes;

    @Column(name = "can_submit_on_behalf_of_self")
    private boolean canSubmitOnBehalfOfSelf;

    @Column(name = "active")
    private boolean active;

    @OneToMany(mappedBy = "visibilityProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<User> users;

    @OneToMany(mappedBy = "visibilityProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<VpGroupLink> onBehalfOfGroups;

    @OneToMany(mappedBy = "visibilityProfile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<VpGroupLink> canViewSubmissionsForGroups;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "visibility_profile_submission_types",
        joinColumns = @JoinColumn(name = "visibility_profile_name"),
        inverseJoinColumns = @JoinColumn(name = "submission_type_id")
    )
    private Set<SubmissionType> canViewSubmissionTypes;
}
