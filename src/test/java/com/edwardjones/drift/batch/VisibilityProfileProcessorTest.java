package com.edwardjones.drift.batch;

import com.edwardjones.drift.domain.VisibilityProfile;
import com.edwardjones.drift.dto.VisibilityProfileJson;
import com.edwardjones.drift.repo.GroupRepository;
import com.edwardjones.drift.repo.SubmissionTypeRepository;
import com.edwardjones.drift.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class VisibilityProfileProcessorTest {

    @Mock private GroupRepository groupRepository;
    @Mock private SubmissionTypeRepository submissionTypeRepository;
    @Mock private UserRepository userRepository;
    private VisibilityProfileProcessor processor;

    @BeforeEach
    void setUp() {
        processor = new VisibilityProfileProcessor(groupRepository, submissionTypeRepository, userRepository);
    }

    @Test
    void process_TransformsVisibilityProfileJsonToVisibilityProfile() throws Exception {
        VisibilityProfileJson vpJson = new VisibilityProfileJson(
            "TestVPName",
            true,  // canOnlyViewInvolvedSubmissions
            false, // canViewAllSubmitterGroups
            true,  // canViewAllSubmissionTypes
            false, // canSubmitOnBehalfOfSelf
            true,  // active
            null,
            null,
            null
        );

        VisibilityProfile result = processor.process(vpJson);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("TestVPName");
        assertThat(result.isCanOnlyViewInvolvedSubmissions()).isTrue();
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void process_HandlesNullInput() throws Exception {
        assertThatThrownBy(() -> processor.process(null))
            .isInstanceOf(NullPointerException.class);
    }
}
