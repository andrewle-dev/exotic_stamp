package metro.ExoticStamp.infra.mail.queue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailQueueServiceTest {

    @Mock
    private JpaMailJobRepository mailJobRepository;

    @InjectMocks
    private MailQueueService mailQueueService;

    @Test
    void enqueueVerifyEmailJob_setsDedupKey() {
        when(mailJobRepository.existsByDedupKey("verify:user@test.com")).thenReturn(false);
        when(mailJobRepository.save(any(MailJob.class))).thenAnswer(inv -> {
            MailJob job = inv.getArgument(0);
            job.setId(UUID.randomUUID());
            return job;
        });

        mailQueueService.enqueueHtmlMail(
                "user@test.com",
                "Verify your account",
                "<p>Click link</p>",
                "verify:user@test.com");

        ArgumentCaptor<MailJob> captor = ArgumentCaptor.forClass(MailJob.class);
        verify(mailJobRepository).save(captor.capture());
        assertEquals("verify:user@test.com", captor.getValue().getDedupKey());
        assertEquals(MailJobStatus.PENDING, captor.getValue().getStatus());
        assertFalse(captor.getValue().getBody().contains("password"));
    }

    @Test
    void enqueueVerifyEmailJob_dedupReturnsExistingId() {
        UUID existingId = UUID.randomUUID();
        MailJob existing = new MailJob();
        existing.setId(existingId);
        when(mailJobRepository.existsByDedupKey("verify:user@test.com")).thenReturn(true);
        when(mailJobRepository.findByDedupKey("verify:user@test.com")).thenReturn(Optional.of(existing));

        UUID result = mailQueueService.enqueueHtmlMail(
                "user@test.com", "Subject", "body", "verify:user@test.com");

        assertEquals(existingId, result);
        verify(mailJobRepository, never()).save(any());
    }

    @Test
    void enqueueOtpJob_withoutDedupKey() {
        when(mailJobRepository.save(any(MailJob.class))).thenAnswer(inv -> {
            MailJob job = inv.getArgument(0);
            job.setId(UUID.randomUUID());
            return job;
        });

        mailQueueService.enqueueHtmlMail("user@test.com", "OTP", "<p>Code: 123456</p>");

        ArgumentCaptor<MailJob> captor = ArgumentCaptor.forClass(MailJob.class);
        verify(mailJobRepository).save(captor.capture());
        assertNull(captor.getValue().getDedupKey());
    }
}
