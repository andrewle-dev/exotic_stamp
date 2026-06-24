package metro.ExoticStamp.infra.mail.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MailQueueService {

    private final JpaMailJobRepository mailJobRepository;

    @Transactional
    public UUID enqueueHtmlMail(String recipient, String subject, String body) {
        return enqueueHtmlMail(recipient, subject, body, null);
    }

    @Transactional
    public UUID enqueueHtmlMail(String recipient, String subject, String body, String dedupKey) {
        if (dedupKey != null && mailJobRepository.existsByDedupKey(dedupKey)) {
            return mailJobRepository.findByDedupKey(dedupKey)
                    .map(MailJob::getId)
                    .orElseThrow();
        }
        try {
            MailJob job = new MailJob();
            job.setRecipient(recipient);
            job.setSubject(subject);
            job.setBody(body);
            job.setDedupKey(dedupKey);
            job.setContentType(MailContentType.HTML);
            job.setStatus(MailJobStatus.PENDING);
            return mailJobRepository.save(job).getId();
        } catch (DataIntegrityViolationException e) {
            log.warn("[MailQueue] enqueue duplicate or constraint recipient={} dedupKey={}: {}",
                    recipient, dedupKey, e.getMessage());
            if (dedupKey != null) {
                return mailJobRepository.findByDedupKey(dedupKey)
                        .map(MailJob::getId)
                        .orElseThrow(() -> e);
            }
            throw e;
        } catch (Exception e) {
            log.warn("[MailQueue] enqueue failed recipient={} dedupKey={}: {}", recipient, dedupKey, e.getMessage());
            throw e;
        }
    }
}
