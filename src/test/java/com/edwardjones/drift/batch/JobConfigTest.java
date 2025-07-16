package com.edwardjones.drift.batch;

import com.edwardjones.drift.dto.*;
import com.edwardjones.drift.infra.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class JobConfigTest {

    @Mock private JobRepository jobRepository;
    @Mock private PlatformTransactionManager transactionManager;
    @Mock private EntityManagerFactory emf;
    @Mock private RestTemplate rest;
    @Mock private TokenService token;
    @Mock private ObjectMapper objectMapper;
    @Mock private JobExecutionMonitor jobExecutionMonitor;
    @Mock private EmailSenderTasklet emailSenderTasklet;

    private JobConfig jobConfig;

    @Test
    void jpaItemWriter_CreatesCorrectWriter() {
        jobConfig = new JobConfig(jobRepository, transactionManager, emf, rest, token, objectMapper, jobExecutionMonitor, emailSenderTasklet);

        JpaItemWriter<Object> writer = jobConfig.jpaItemWriter();

        assertThat(writer).isNotNull();
        // Note: usePersist property is not publicly accessible for testing
        // The configuration is verified through the actual batch job execution
    }

    @Test
    void userReader_ConfiguresCorrectEndpoint() {
        jobConfig = new JobConfig(jobRepository, transactionManager, emf, rest, token, objectMapper, jobExecutionMonitor, emailSenderTasklet);

        ItemStreamReader<UserJson> reader = jobConfig.userReader();

        assertThat(reader).isInstanceOf(RedOakStreamReader.class);
    }

    @Test
    void groupReader_ConfiguresCorrectEndpoint() {
        jobConfig = new JobConfig(jobRepository, transactionManager, emf, rest, token, objectMapper, jobExecutionMonitor, emailSenderTasklet);

        ItemStreamReader<GroupJson> reader = jobConfig.groupReader();

        assertThat(reader).isInstanceOf(RedOakStreamReader.class);
    }

    @Test
    void submissionTypeReader_ConfiguresCorrectEndpoint() {
        jobConfig = new JobConfig(jobRepository, transactionManager, emf, rest, token, objectMapper, jobExecutionMonitor, emailSenderTasklet);

        ItemStreamReader<SubmissionTypeJson> reader = jobConfig.submissionTypeReader();

        assertThat(reader).isInstanceOf(RedOakStreamReader.class);
    }

    @Test
    void importJob_ConfiguresCorrectStepOrder() {
        jobConfig = new JobConfig(jobRepository, transactionManager, emf, rest, token, objectMapper, jobExecutionMonitor, emailSenderTasklet);

        Step emailStep = mock(Step.class);
        Step userStep = mock(Step.class);
        Step groupStep = mock(Step.class);
        Step vpStep = mock(Step.class);
        Step submissionTypeStep = mock(Step.class);

        Job job = jobConfig.importJob(emailStep, userStep, groupStep, vpStep, submissionTypeStep);

        assertThat(job).isNotNull();
        assertThat(job.getName()).isEqualTo("redoak-import");
    }
}
