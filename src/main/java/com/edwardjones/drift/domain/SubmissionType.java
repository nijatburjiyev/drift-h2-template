package com.edwardjones.drift.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class SubmissionType {
    @Id
    private Long id;

    private String name;

    @Column(columnDefinition = "CLOB")
    private String description;

    @Column(columnDefinition = "CLOB")
    private String helpText;

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
}
