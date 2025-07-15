package com.edwardjones.drift.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.Set;

@Entity
@Table(name = "groups")
@Data
@EqualsAndHashCode(exclude = {"users"})
@ToString(exclude = {"users"})
public class Group {
    @Id
    @Column(name = "group_name")
    private String groupName;

    @Column(name = "group_description")
    private String groupDescription;

    @Column(name = "active")
    private boolean active;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "group_users",
        joinColumns = @JoinColumn(name = "group_name"),
        inverseJoinColumns = @JoinColumn(name = "user_name")
    )
    private Set<User> users;
}
