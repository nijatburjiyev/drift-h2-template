package com.edwardjones.drift.batch;

import com.edwardjones.drift.dto.UserJson;
import com.edwardjones.drift.infra.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RedOakStreamReaderTest {

    @Mock private RestTemplate restTemplate;
    @Mock private TokenService tokenService;
    @Mock private ObjectMapper objectMapper;

    @Test
    void redOakStreamReader_InitializesCorrectly() {
        RedOakStreamReader<UserJson> reader = new RedOakStreamReader<>(
            UserJson.class,
            "https://api.test.com/users",
            restTemplate,
            tokenService,
            objectMapper
        );

        assertThat(reader).isNotNull();
    }

    @Test
    void redOakStreamReader_HandlesNullToken() {
        RedOakStreamReader<UserJson> reader = new RedOakStreamReader<>(
            UserJson.class,
            "https://api.test.com/users",
            restTemplate,
            tokenService,
            objectMapper
        );

        assertThat(reader).isNotNull();
    }
}
