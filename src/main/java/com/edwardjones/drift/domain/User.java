package com.edwardjones.drift.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(exclude = {"groups", "visibilityProfile"}, callSuper = false)
@ToString(exclude = {"groups", "visibilityProfile"})
public class User extends AuditFields {
    @Id
    @Column(name = "user_name")
    private String userName;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "active")
    private boolean active;

    @Column(name = "time_zone")
    private String timeZone;

    @Column(name = "locale")
    private String locale;

    @Column(name = "landing_page")
    private String landingPage;

    @Column(name = "restrict_by_ip_address")
    private boolean restrictByIpAddress;

    @Column(name = "sso_only")
    private boolean ssoOnly;

    @ElementCollection
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_name"))
    @Column(name = "role")
    private Set<String> roles;

    @ElementCollection
    @CollectionTable(name = "user_admin_roles", joinColumns = @JoinColumn(name = "user_name"))
    @Column(name = "admin_role")
    private Set<String> adminRoles;

    @ElementCollection
    @CollectionTable(name = "user_admin_role_permissions", joinColumns = @JoinColumn(name = "user_name"))
    @Column(name = "permission")
    private Set<String> adminRolePermissions;

    @ManyToMany(mappedBy = "users", fetch = FetchType.LAZY)
    private Set<Group> groups;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visibility_profile_name")
    private VisibilityProfile visibilityProfile;
}
