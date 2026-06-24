package metro.ExoticStamp.infra.mail.queue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailWorkerTest {

    @Mock private MailJobProcessor mailJobProcessor;
    @Mock private JpaMailJobRepository mailJobRepository;

    @InjectMocks
    private MailWorker mailWorker;

    @Test
    void processBatch_invokesProcessorBatchSizeTimes() {
        ReflectionTestUtils.setField(mailWorker, "batchSize", 3);
        mailWorker.processBatch();
        verify(mailJobProcessor, times(3)).processNextPendingJobIfAny();
    }

    @Test
    void resetStuckProcessingJobs_delegatesToRepository() {
        ReflectionTestUtils.setField(mailWorker, "stuckProcessingThresholdMinutes", 5);
        mailWorker.resetStuckProcessingJobs();
        verify(mailJobRepository).resetStuckJobs(any(LocalDateTime.class), any(LocalDateTime.class), anyString());
    }
}
