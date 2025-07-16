package com.edwardjones.drift.batch;

import com.edwardjones.drift.domain.*;
import com.edwardjones.drift.dto.*;
import com.edwardjones.drift.infra.TokenService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.client.RestTemplate;

@Configuration
@RequiredArgsConstructor
public class JobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory emf;
    private final RestTemplate rest;
    private final TokenService token;
    private final ObjectMapper objectMapper;
    private final JobExecutionMonitor jobExecutionMonitor;
    private final EmailSenderTasklet emailSenderTasklet;

    /* ——— generic JPA writer ——— */
    @Bean
    public <T> JpaItemWriter<T> jpaItemWriter() {
        var writer = new JpaItemWriter<T>();
        writer.setEntityManagerFactory(emf);
        writer.setUsePersist(false);  // Use merge() instead of persist() to handle existing entities
        return writer;
    }

    /* ——— readers ——— */
    @Bean
    @StepScope
    ItemStreamReader<UserJson> userReader() {
        return new RedOakStreamReader<>(UserJson.class,
                "https://api.redoak.example.com/api/v1/users", rest, token, objectMapper);
    }

    @Bean
    @StepScope
    ItemStreamReader<GroupJson> groupReader() {
        return new RedOakStreamReader<>(GroupJson.class,
                "https://api.redoak.example.com/api/v1/groups", rest, token, objectMapper);
    }

    @Bean
    @StepScope
    ItemStreamReader<VisibilityProfileJson> vpReader() {
        return new RedOakStreamReader<>(VisibilityProfileJson.class,
                "https://api.redoak.example.com/api/v1/visibilityProfiles", rest, token, objectMapper);
    }

    @Bean
    @StepScope
    ItemStreamReader<SubmissionTypeJson> submissionTypeReader() {
        return new RedOakStreamReader<>(SubmissionTypeJson.class,
                "https://api.redoak.example.com/api/v1/submissionTypes", rest, token, objectMapper);
    }

    /* ——— steps ——— */
    @Bean
    Step loadUsers(ItemStreamReader<UserJson> userReader,
                   ItemProcessor<UserJson,User> userProcessor) {
        return new StepBuilder("loadUsers", jobRepository)
                .<UserJson,User>chunk(1000, transactionManager)
                .reader(userReader)
                .processor(userProcessor)
                .writer(jpaItemWriter())
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .skipLimit(10)
                .skip(Exception.class)
                .build();
    }

    @Bean
    Step loadGroups(ItemStreamReader<GroupJson> groupReader,
                    ItemProcessor<GroupJson,Group> groupProcessor) {
        return new StepBuilder("loadGroups", jobRepository)
                .<GroupJson,Group>chunk(500, transactionManager)
                .reader(groupReader)
                .processor(groupProcessor)
                .writer(jpaItemWriter())
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .skipLimit(10)
                .skip(Exception.class)
                .build();
    }

    @Bean
    Step loadVPs(ItemStreamReader<VisibilityProfileJson> vpReader,
                 ItemProcessor<VisibilityProfileJson,VisibilityProfile> vpProcessor) {
        return new StepBuilder("loadVPs", jobRepository)
                .<VisibilityProfileJson,VisibilityProfile>chunk(250, transactionManager)
                .reader(vpReader)
                .processor(vpProcessor)
                .writer(jpaItemWriter())
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .skipLimit(10)
                .skip(Exception.class)
                .build();
    }

    @Bean
    Step loadSubmissionTypes(ItemStreamReader<SubmissionTypeJson> submissionTypeReader,
                            ItemProcessor<SubmissionTypeJson,SubmissionType> submissionTypeProcessor) {
        return new StepBuilder("loadSubmissionTypes", jobRepository)
                .<SubmissionTypeJson,SubmissionType>chunk(500, transactionManager)
                .reader(submissionTypeReader)
                .processor(submissionTypeProcessor)
                .writer(jpaItemWriter())
                .faultTolerant()
                .retryLimit(3)
                .retry(Exception.class)
                .skipLimit(10)
                .skip(Exception.class)
                .build();
    }

    @Bean
    Step emailSenderStep() {
        return new StepBuilder("emailSenderStep", jobRepository)
                .tasklet(emailSenderTasklet, transactionManager)
                .build();
    }

    /* ——— job ——— */
    @Bean
    Job importJob(Step emailSenderStep, Step loadUsers, Step loadGroups, Step loadVPs, Step loadSubmissionTypes) {
        return new JobBuilder("redoak-import", jobRepository)
                   .listener(jobExecutionMonitor)  // Add job-level monitoring
                   .start(emailSenderStep)         // send email first
                   .next(loadUsers)               // users first
                   .next(loadSubmissionTypes)     // submission types before VPs need them
                   .next(loadGroups)              // groups can now resolve users
                   .next(loadVPs)                 // VPs need both users and submission types
                   .build();
    }
}
