package com.example.demo.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;

import lombok.Data;
import lombok.NoArgsConstructor;


@Entity
@Data
@NoArgsConstructor
public class User {
    @Id
    @Column(length = 128)
    private String userName;
    private String firstName;
    private String lastName;
    private String emailAddress;
    private boolean active;
    private String timeZone;
    private String locale;

    @ManyToMany(mappedBy = "users")
    private Set<Group> groups = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visibility_profile_name")
    private VisibilityProfile visibilityProfile;
}
