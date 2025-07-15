package com.edwardjones.drift.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.assertj.core.api.Assertions.assertThat;

class SubmissionTypeTest {

    private SubmissionType submissionType;

    @BeforeEach
    void setUp() {
        submissionType = new SubmissionType();
    }

    @Test
    void submissionType_HasCorrectDefaultValues() {
        assertThat(submissionType.getId()).isNull();
        assertThat(submissionType.getName()).isNull();
        assertThat(submissionType.getDescription()).isNull();
        assertThat(submissionType.getHelpText()).isNull();
        assertThat(submissionType.isActive()).isFalse();
        assertThat(submissionType.getInitialState()).isNull();
        assertThat(submissionType.getPriority()).isEqualTo(0);
    }

    @Test
    void submissionType_SettersAndGettersWork() {
        submissionType.setId(1L);
        submissionType.setName("Test Type");
        submissionType.setDescription("Test Description");
        submissionType.setHelpText("Test Help");
        submissionType.setActive(true);
        submissionType.setInitialState("DRAFT");
        submissionType.setPriority(10);

        assertThat(submissionType.getId()).isEqualTo(1L);
        assertThat(submissionType.getName()).isEqualTo("Test Type");
        assertThat(submissionType.getDescription()).isEqualTo("Test Description");
        assertThat(submissionType.getHelpText()).isEqualTo("Test Help");
        assertThat(submissionType.isActive()).isTrue();
        assertThat(submissionType.getInitialState()).isEqualTo("DRAFT");
        assertThat(submissionType.getPriority()).isEqualTo(10);
    }

    @Test
    void submissionType_ExtendsAuditFields() {
        assertThat(submissionType).isInstanceOf(AuditFields.class);
    }
}
