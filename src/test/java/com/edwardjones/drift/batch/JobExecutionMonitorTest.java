package com.edwardjones.drift.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JobExecutionMonitorTest {

    @Mock private JobExecution jobExecution;
    @Mock private JobParameters jobParameters;
    @Mock private StepExecution stepExecution;

    @Test
    void jobExecutionMonitor_ImplementsJobExecutionListener() {
        JobExecutionMonitor monitor = new JobExecutionMonitor();

        assertThat(monitor).isInstanceOf(JobExecutionListener.class);
    }

    @Test
    void beforeJob_HandlesJobExecution() {
        JobExecutionMonitor monitor = new JobExecutionMonitor();
        when(jobExecution.getJobId()).thenReturn(123L);
        when(jobExecution.getJobParameters()).thenReturn(jobParameters);

        // Should not throw exception
        monitor.beforeJob(jobExecution);
    }

    @Test
    void afterJob_HandlesJobExecution() {
        JobExecutionMonitor monitor = new JobExecutionMonitor();
        when(jobExecution.getJobId()).thenReturn(123L);
        when(jobExecution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        when(jobExecution.getStepExecutions()).thenReturn(Collections.singletonList(stepExecution));
        when(stepExecution.getStepName()).thenReturn("testStep");
        when(stepExecution.getReadCount()).thenReturn(10L);
        when(stepExecution.getWriteCount()).thenReturn(10L);
        when(stepExecution.getSkipCount()).thenReturn(0L);

        // Should not throw exception
        monitor.afterJob(jobExecution);
    }

    @Test
    void afterJob_HandlesFailedJob() {
        JobExecutionMonitor monitor = new JobExecutionMonitor();
        when(jobExecution.getJobId()).thenReturn(123L);
        when(jobExecution.getStatus()).thenReturn(BatchStatus.FAILED);
        when(jobExecution.getStepExecutions()).thenReturn(Collections.emptyList());
        when(jobExecution.getExitStatus()).thenReturn(new ExitStatus("FAILED", "Test failure"));

        // Should not throw exception
        monitor.afterJob(jobExecution);
    }
}
