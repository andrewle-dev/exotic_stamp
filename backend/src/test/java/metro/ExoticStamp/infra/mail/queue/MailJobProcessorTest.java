package metro.ExoticStamp.infra.mail.queue;

import metro.ExoticStamp.infra.mail.MailProperties;
import metro.ExoticStamp.infra.mail.MailSenderPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailJobProcessorTest {

    @Mock private JpaMailJobRepository repo;
    @Mock private MailSenderPort senderPort;
    @Mock private MailRateLimiter rateLimiter;
    @Mock private MailProperties mailProperties;

    @InjectMocks
    private MailJobProcessor processor;

    @Test
    void processJob_success_marksSent() throws Exception {
        MailJob job = new MailJob();
        job.setId(UUID.randomUUID());
        job.setRecipient("u@test.com");
        job.setSubject("Hi");
        job.setBody("body");
        job.setContentType(MailContentType.HTML);
        job.setStatus(MailJobStatus.PENDING);
        when(repo.findPendingJobsForUpdate(any(), eq(1))).thenReturn(List.of(job));
        when(rateLimiter.tryAcquire()).thenReturn(true);
        when(mailProperties.getFrom()).thenReturn("noreply@test.com");

        processor.processNextPendingJobIfAny();

        assertEquals(MailJobStatus.SENT, job.getStatus());
        assertNotNull(job.getProcessedAt());
        verify(senderPort).send(any());
    }

    @Test
    void processJob_failure_schedulesRetry() throws Exception {
        MailJob job = new MailJob();
        job.setId(UUID.randomUUID());
        job.setRecipient("u@test.com");
        job.setSubject("Hi");
        job.setBody("body");
        job.setContentType(MailContentType.HTML);
        job.setStatus(MailJobStatus.PENDING);
        job.setRetryCount(0);
        job.setMaxRetries(3);
        when(repo.findPendingJobsForUpdate(any(), eq(1))).thenReturn(List.of(job));
        when(rateLimiter.tryAcquire()).thenReturn(true);
        when(mailProperties.getFrom()).thenReturn("noreply@test.com");
        doThrow(new RuntimeException("smtp down")).when(senderPort).send(any());

        processor.processNextPendingJobIfAny();

        assertEquals(MailJobStatus.PENDING, job.getStatus());
        assertEquals(1, job.getRetryCount());
        assertNotNull(job.getNextRetryAt());
    }

    @Test
    void processJob_maxRetries_marksDead() throws Exception {
        MailJob job = new MailJob();
        job.setId(UUID.randomUUID());
        job.setRecipient("u@test.com");
        job.setSubject("Hi");
        job.setBody("body");
        job.setContentType(MailContentType.HTML);
        job.setStatus(MailJobStatus.PENDING);
        job.setRetryCount(3);
        job.setMaxRetries(3);
        when(repo.findPendingJobsForUpdate(any(), eq(1))).thenReturn(List.of(job));
        when(rateLimiter.tryAcquire()).thenReturn(true);
        when(mailProperties.getFrom()).thenReturn("noreply@test.com");
        doThrow(new RuntimeException("smtp down")).when(senderPort).send(any());

        processor.processNextPendingJobIfAny();

        assertEquals(MailJobStatus.DEAD, job.getStatus());
    }
}
