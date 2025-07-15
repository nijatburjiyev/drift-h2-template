package com.example.demo.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;

import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
public class SubmissionType {
    @Id
    private Long id;
    private String name;
    @Column(columnDefinition = "CLOB")
    private String description;
    private boolean active;
    private String initialState;
    private int priority;

    @ElementCollection
    @CollectionTable(name = "SUBMISSION_TYPE_STATES",
                     joinColumns = @JoinColumn(name = "submission_type_id"))
    private List<String> approvedStates = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "SUBMISSION_TYPE_FORMATS",
                     joinColumns = @JoinColumn(name = "submission_type_id"))
    private List<String> formats = new ArrayList<>();

    @Column(columnDefinition = "CLOB")
    private String helpText;
}
