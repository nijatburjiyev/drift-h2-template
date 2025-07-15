package com.edwardjones.drift.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

/**
 * Email sender tasklet that runs before the main import job.
 * This placeholder can be extended to send notifications about job start,
 * pre-process validation results, or other email communications.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EmailSenderTasklet implements Tasklet {

    // TODO: Inject email service when implemented
    // private final EmailService emailService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("Starting email sender tasklet...");

        // TODO: Implement email sending logic
        // Examples of what this tasklet could do:
        // 1. Send job start notification to administrators
        // 2. Send pre-validation report
        // 3. Send daily/weekly job summary
        // 4. Send error notifications from previous runs

        try {
            // Placeholder implementation
            sendJobStartNotification();

            log.info("Email sender tasklet completed successfully");
            return RepeatStatus.FINISHED;

        } catch (Exception e) {
            log.error("Email sender tasklet failed", e);
            throw e;
        }
    }

    /**
     * Placeholder method for sending job start notification.
     * This should be replaced with actual email service implementation.
     */
    private void sendJobStartNotification() {
        // TODO: Replace with actual email implementation
        log.info("PLACEHOLDER: Would send job start notification email");
        log.info("PLACEHOLDER: Recipients: admin@company.com, ops-team@company.com");
        log.info("PLACEHOLDER: Subject: RedOak Import Job Started");
        log.info("PLACEHOLDER: Body: The daily RedOak data import job has started at {}",
                java.time.LocalDateTime.now());

        // Simulate email sending delay
        try {
            Thread.sleep(1000); // 1 second delay
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
