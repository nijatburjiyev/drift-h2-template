package com.edwardjones.drift.batch;

import com.edwardjones.drift.infra.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
@ActiveProfiles("test")
@EnableBatchProcessing
class BatchConfigurationIntegrationTest {

    @MockitoBean private RestTemplate restTemplate;
    @MockitoBean private TokenService tokenService;

    @Test
    void batchConfiguration_LoadsAllBeansCorrectly() {
        // This test verifies that all batch beans are properly configured
        // and can be loaded in the Spring context
        assertThat(true).isTrue(); // Context loading is the actual test
    }
}
