package metro.ExoticStamp.infra.mail.queue;

import metro.ExoticStamp.infra.mail.MailMessage;
import metro.ExoticStamp.infra.mail.MailProperties;
import metro.ExoticStamp.infra.mail.MailSenderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailJobProcessor {

    private final JpaMailJobRepository repo;
    private final MailSenderPort senderPort;
    private final MailRateLimiter rateLimiter;
    private final MailProperties mailProperties;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processNextPendingJobIfAny() {
        List<MailJob> jobs = repo.findPendingJobsForUpdate(LocalDateTime.now(), 1);
        if (jobs.isEmpty()) {
            return;
        }
        MailJob job = jobs.get(0);
        try {
            if (!rateLimiter.tryAcquire()) {
                log.warn("Mail rate limit hit — skipping job {}", job.getId());
                return;
            }
            job.setStatus(MailJobStatus.PROCESSING);
            repo.save(job);

            MailMessage msg = new MailMessage(
                    mailProperties.getFrom(),
                    job.getRecipient(),
                    job.getSubject(),
                    job.getBody(),
                    job.getContentType()
            );
            senderPort.send(msg);

            job.setStatus(MailJobStatus.SENT);
            job.setProcessedAt(LocalDateTime.now());
            repo.save(job);
        } catch (Exception e) {
            int beforeIncrement = job.getRetryCount();
            log.error("Mail send failed for job {} (attempt {}): {}",
                    job.getId(), beforeIncrement + 1, e.getMessage());
            job.setRetryCount(beforeIncrement + 1);
            job.setLastError(e.getMessage());
            if (job.isRetryable()) {
                job.setStatus(MailJobStatus.PENDING);
                job.setNextRetryAt(MailRetryPolicy.nextRetryAt(beforeIncrement));
            } else {
                job.setStatus(MailJobStatus.DEAD);
            }
            repo.save(job);
        }
    }
}
