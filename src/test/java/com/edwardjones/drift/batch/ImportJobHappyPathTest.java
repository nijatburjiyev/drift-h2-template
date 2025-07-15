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
 * Focused test to validate that our fixes work correctly.
 * Tests the core functionality without complex relationship checking.
 */
@SpringBootTest
@SpringBatchTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.batch.job.enabled=false",
        "spring.h2.console.enabled=true"
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
    void jobLoadsDataCorrectly() throws Exception {
        var params = new JobParametersBuilder()
                .addLong("run.id", System.currentTimeMillis())
                .toJobParameters();

        var exec = jobLauncherTestUtils.launchJob(params);
        assertThat(exec.getExitStatus().getExitCode()).isEqualTo("COMPLETED");

        // Validate data was loaded
        assertThat(users.count()).isPositive();
        assertThat(groups.count()).isPositive();
        assertThat(vps.count()).isPositive();

        // Print results for verification
        System.out.println("\n=== DATA VALIDATION RESULTS ===");
        System.out.println("✅ Job completed successfully");
        System.out.println("✅ Users loaded: " + users.count());
        System.out.println("✅ Groups loaded: " + groups.count());
        System.out.println("✅ Visibility Profiles loaded: " + vps.count());
        System.out.println("✅ All steps executed in correct order");

        System.out.println("\n=== DATABASE ACCESS INFO ===");
        System.out.println("H2 Console URL: http://localhost:8080/h2");
        System.out.println("JDBC URL: jdbc:h2:mem:testdb");
        System.out.println("Username: sa");
        System.out.println("Password: (empty)");
        System.out.println("============================");

        // Uncomment the next line to pause execution and manually inspect the database
        // Thread.sleep(300000); // 5 minutes to manually inspect

        mockServer.verify();
    }

    /* helper ─────────────────────────────────────────────────────────────── */
    private static String read(String classpathFile) throws Exception {
        return Files.readString(Paths.get(new ClassPathResource(classpathFile).getURI()));
    }
}
