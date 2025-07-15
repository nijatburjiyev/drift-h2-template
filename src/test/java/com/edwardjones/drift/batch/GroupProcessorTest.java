package com.edwardjones.drift.batch;

import com.edwardjones.drift.domain.Group;
import com.edwardjones.drift.dto.GroupJson;
import com.edwardjones.drift.repo.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class GroupProcessorTest {

    @Mock
    private UserRepository userRepository;
    private GroupProcessor groupProcessor;

    @BeforeEach
    void setUp() {
        groupProcessor = new GroupProcessor(userRepository);
    }

    @Test
    void process_TransformsGroupJsonToGroup() throws Exception {
        GroupJson groupJson = new GroupJson(
            "Test Group", "Test Description", true, null
        );

        Group result = groupProcessor.process(groupJson);

        assertThat(result).isNotNull();
        assertThat(result.getGroupName()).isEqualTo("Test Group");
        assertThat(result.getGroupDescription()).isEqualTo("Test Description");
        assertThat(result.isActive()).isTrue();
    }

    @Test
    void process_HandlesNullInput() throws Exception {
        assertThatThrownBy(() -> groupProcessor.process(null))
            .isInstanceOf(NullPointerException.class);
    }
}
