package com.edwardjones.drift.batch;

import com.edwardjones.drift.repo.GroupRepository;
import com.edwardjones.drift.repo.UserRepository;
import com.edwardjones.drift.repo.VisibilityProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.nio.file.Files;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

/**
 * Smallest–possible "does-it-load?" test.
 * – fakes the vendor endpoints with MockRestServiceServer
 * – launches the Spring-Batch job once
 * – checks that at least one row landed in each table
 */
@SpringBootTest
@SpringBatchTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.batch.job.enabled=false"  // Disable automatic job execution
})
class ImportJobHappyPathTest {

    @Autowired private JobLauncherTestUtils jobLauncherTestUtils;
    @Autowired private RestTemplate            restTemplate;
    @Autowired private UserRepository          users;
    @Autowired private GroupRepository         groups;
    @Autowired private VisibilityProfileRepository vps;

    private MockRestServiceServer mockServer;

    @BeforeEach
    void wireMocks() throws Exception {
        mockServer = MockRestServiceServer.createServer(restTemplate);

        mockServer.expect(requestTo("https://api.redoak.example.com/getToken"))
                  .andRespond(withSuccess("{\"access_token\":\"test-token\"}", APPLICATION_JSON));

        // Fix order to match job execution: users -> submissionTypes -> groups -> visibilityProfiles
        mockServer.expect(requestTo("https://api.redoak.example.com/api/v1/users"))
                  .andRespond(withSuccess(read("mock-users.json"), APPLICATION_JSON));

        mockServer.expect(requestTo("https://api.redoak.example.com/api/v1/submissionTypes"))
                  .andRespond(withSuccess("[]", APPLICATION_JSON));  // Empty array for PoC

        mockServer.expect(requestTo("https://api.redoak.example.com/api/v1/groups"))
                  .andRespond(withSuccess(read("mock-groups.json"), APPLICATION_JSON));

        mockServer.expect(requestTo("https://api.redoak.example.com/api/v1/visibilityProfiles"))
                  .andRespond(withSuccess(read("mock-visibility-profiles.json"), APPLICATION_JSON));
    }

    @Test
    void jobLoadsSomething() throws Exception {
        var params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())   // unique run
                .toJobParameters();

        var exec = jobLauncherTestUtils.launchJob(params);
        assertThat(exec.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        assertThat(users.count()).isPositive();
        assertThat(groups.count()).isPositive();
        assertThat(vps.count()).isPositive();

        mockServer.verify();           // all mocked calls were hit exactly once
    }

    /* helper ─────────────────────────────────────────────────────────────── */
    private static String read(String classpathFile) throws Exception {
        return Files.readString(Paths.get(new ClassPathResource(classpathFile).getURI()));
    }
}
