package com.edwardjones.drift.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class SubmissionType extends AuditFields {
    @Id
    private Long id;

    private String name;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String description;

    @Lob
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
