package com.edwardjones.drift.batch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class EmailSenderTaskletTest {

    @Mock private StepContribution stepContribution;
    @Mock private ChunkContext chunkContext;

    @Test
    void execute_ReturnsFinished() throws Exception {
        EmailSenderTasklet tasklet = new EmailSenderTasklet();

        RepeatStatus status = tasklet.execute(stepContribution, chunkContext);

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }

    @Test
    void execute_HandlesNullParameters() throws Exception {
        EmailSenderTasklet tasklet = new EmailSenderTasklet();

        RepeatStatus status = tasklet.execute(null, null);

        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
    }
}
