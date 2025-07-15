package com.example.demo.domain;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "USER_GROUP")
public class Group {
    @Id
    @Column(length = 128)
    private String groupName;

    private String groupDescription;
    private boolean active;

    @ManyToMany
    @JoinTable(name = "GROUP_USER_XREF",
               joinColumns        = @JoinColumn(name = "group_name"),
               inverseJoinColumns = @JoinColumn(name = "user_name"))
    private Set<User> users = new HashSet<>();
}
