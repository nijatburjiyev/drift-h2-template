package com.edwardjones.drift.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.io.Serializable;

@Entity
@Table(name = "vp_group_links")
@Data
@EqualsAndHashCode(exclude = {"visibilityProfile"})
@ToString(exclude = {"visibilityProfile"})
@IdClass(VpGroupLink.VpGroupId.class)
public class VpGroupLink {

    @Id
    @Column(name = "visibility_profile_name")
    private String visibilityProfileName;

    @Id
    @Column(name = "group_name")
    private String groupName;

    @Id
    @Column(name = "link_type")
    @Enumerated(EnumType.STRING)
    private Role linkType;

    @Column(name = "active")
    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visibility_profile_name", insertable = false, updatable = false)
    private VisibilityProfile visibilityProfile;

    // Convenience setter that accepts String and converts to enum
    public void setLinkType(String linkType) {
        this.linkType = Role.valueOf(linkType);
    }

    // Convenience setter that sets the visibility profile and updates the name
    public void setVisibilityProfile(VisibilityProfile vp) {
        this.visibilityProfile = vp;
        this.visibilityProfileName = vp != null ? vp.getName() : null;
    }

    @Data
    public static class VpGroupId implements Serializable {
        private String visibilityProfileName;
        private String groupName;
        private Role linkType;
    }

    public enum Role {
        ON_BEHALF_OF,
        CAN_VIEW
    }
}
