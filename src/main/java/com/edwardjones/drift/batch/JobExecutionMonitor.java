package com.edwardjones.drift.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class JobExecutionMonitor implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("Starting redoak-import job: {}", jobExecution.getJobId());
        log.info("Job parameters: {}", jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("Completed redoak-import job: {} - Status: {}",
            jobExecution.getJobId(), jobExecution.getStatus());

        // Log step statistics
        jobExecution.getStepExecutions().forEach(step -> {
            log.info("Step '{}' - Read: {}, Written: {}, Skipped: {}",
                step.getStepName(), step.getReadCount(), step.getWriteCount(), step.getSkipCount());
        });

        if (jobExecution.getStatus().isUnsuccessful()) {
            log.error("Job failed with exit code: {}", jobExecution.getExitStatus().getExitCode());
        }
    }
}
