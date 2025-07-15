package com.example.demo.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class VisibilityProfile {
    @Id
    @Column(length = 128)
    private String name;

    private boolean canOnlyViewInvolvedSubmissions;
    private boolean canViewAllSubmitterGroups;
    private boolean canViewAllSubmissionTypes;
    private boolean canSubmitOnBehalfOfSelf;
    private boolean active;

    @ManyToMany
    @JoinTable(name = "VP_SUBMISSION_TYPE_XREF",
               joinColumns        = @JoinColumn(name = "vp_name"),
               inverseJoinColumns = @JoinColumn(name = "submission_type_id"))
    private Set<SubmissionType> specificSubmissionTypes = new HashSet<>();

    @OneToMany(mappedBy = "profile",
               cascade = CascadeType.ALL,
               orphanRemoval = true)
    private Set<VpGroupLink> groupLinks = new HashSet<>();
}
