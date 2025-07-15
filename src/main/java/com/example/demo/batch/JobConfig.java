package com.example.demo.batch;

import jakarta.persistence.EntityManagerFactory;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.demo.domain.Group;
import com.example.demo.domain.User;
import com.example.demo.domain.VisibilityProfile;
import com.example.demo.dto.GroupJson;
import com.example.demo.dto.UserJson;
import com.example.demo.dto.VisibilityProfileJson;
import com.example.demo.batch.GroupProcessor;
import com.example.demo.batch.UserProcessor;
import com.example.demo.batch.VisibilityProfileProcessor;
import com.example.demo.infra.TokenService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.client.RestTemplate;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class JobConfig {

    private final JobBuilderFactory  jobs;
    private final StepBuilderFactory steps;
    private final EntityManagerFactory emf;
    private final RestTemplate rest;
    private final TokenService token;

    private <T> JpaItemWriter<T> writer() {
        var w = new JpaItemWriter<T>();
        w.setEntityManagerFactory(emf);
        return w;
    }

    @Bean
    ItemStreamReader<UserJson> userReader() {
        return new RedOakStreamReader<>(UserJson.class,
               "https://api.redoak.example.com/api/v1/users", rest, token);
    }
    @Bean
    ItemStreamReader<GroupJson> groupReader() {
        return new RedOakStreamReader<>(GroupJson.class,
               "https://api.redoak.example.com/api/v1/groups", rest, token);
    }
    @Bean
    ItemStreamReader<VisibilityProfileJson> vpReader() {
        return new RedOakStreamReader<>(VisibilityProfileJson.class,
               "https://api.redoak.example.com/api/v1/visibilityProfiles", rest, token);
    }

    @Bean
    Step loadUsers(ItemStreamReader<UserJson> userReader,
                   ItemProcessor<UserJson,User> userProcessor) {
        return steps.get("loadUsers")
                    .<UserJson,User>chunk(1000)
                    .reader(userReader)
                    .processor(userProcessor)
                    .writer(writer())
                    .build();
    }
    @Bean
    Step loadGroups(ItemStreamReader<GroupJson> groupReader,
                    ItemProcessor<GroupJson,Group> groupProcessor) {
        return steps.get("loadGroups")
                    .<GroupJson,Group>chunk(500)
                    .reader(groupReader)
                    .processor(groupProcessor)
                    .writer(writer())
                    .build();
    }
    @Bean
    Step loadVPs(ItemStreamReader<VisibilityProfileJson> vpReader,
                 ItemProcessor<VisibilityProfileJson,VisibilityProfile> vpProcessor) {
        return steps.get("loadVPs")
                    .<VisibilityProfileJson,VisibilityProfile>chunk(250)
                    .reader(vpReader)
                    .processor(vpProcessor)
                    .writer(writer())
                    .build();
    }

    @Bean
    Job importJob(Step loadUsers, Step loadGroups, Step loadVPs) {
        return jobs.get("redoak-import")
                   .start(loadUsers)
                   .next(loadGroups)
                   .next(loadVPs)
                   .build();
    }
}
