package com.example.demo.domain;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "VP_GROUP_LINK",
       uniqueConstraints = @UniqueConstraint(
           name = "UK_VP_GROUP_ROLE",
           columnNames = {"profile_name","group_name","role"}))
public class VpGroupLink {

    @EmbeddedId
    private VpGroupId id;

    @MapsId("profileName")
    @ManyToOne
    @JoinColumn(name = "profile_name")
    private VisibilityProfile profile;

    @MapsId("groupName")
    @ManyToOne
    @JoinColumn(name = "group_name")
    private Group group;

    @Enumerated(EnumType.STRING)
    private Role role;      // ON_BEHALF_OF, CAN_VIEW
    private boolean active;

    @Embeddable
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VpGroupId implements Serializable {
        private String profileName;
        private String groupName;
    }
    public enum Role { ON_BEHALF_OF, CAN_VIEW }
}
