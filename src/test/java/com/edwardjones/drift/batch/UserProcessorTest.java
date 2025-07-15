package com.edwardjones.drift.batch;

import com.edwardjones.drift.domain.User;
import com.edwardjones.drift.dto.UserJson;
import com.edwardjones.drift.repo.VisibilityProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UserProcessorTest {

    @Mock
    private VisibilityProfileRepository vpRepo;
    private UserProcessor userProcessor;

    @BeforeEach
    void setUp() {
        userProcessor = new UserProcessor(vpRepo);
    }

    @Test
    void process_TransformsUserJsonToUser() {
        UserJson userJson = new UserJson(
            "testuser", "John", "Doe", "test@example.com", true,
            "UTC", "en", null,
            null, null, null,
            "HOME", false, false,
            null
        );

        User result = userProcessor.process(userJson);

        assertThat(result.getUserName()).isEqualTo("testuser");
        assertThat(result.getFirstName()).isEqualTo("John");
        assertThat(result.getLastName()).isEqualTo("Doe");
        assertThat(result.getEmailAddress()).isEqualTo("test@example.com");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getTimeZone()).isEqualTo("UTC");
        assertThat(result.getLocale()).isEqualTo("en");
        assertThat(result.getLandingPage()).isEqualTo("HOME");
        assertThat(result.isRestrictByIpAddress()).isFalse();
        assertThat(result.isSsoOnly()).isFalse();
    }

    @Test
    void process_NullInput_ThrowsException() {
        assertThatThrownBy(() -> userProcessor.process(null))
            .isInstanceOf(NullPointerException.class);
    }
}
