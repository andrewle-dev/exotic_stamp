package metro.ExoticStamp.infra.mail.queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class MailWorker {

    private static final String STUCK_RECOVERY_MESSAGE = "Stuck in PROCESSING — recovered";

    private final MailJobProcessor mailJobProcessor;
    private final JpaMailJobRepository mailJobRepository;

    @Value("${application.mail.queue.batch-size:10}")
    private int batchSize;

    @Value("${application.mail.queue.stuck-processing-threshold-minutes:5}")
    private int stuckProcessingThresholdMinutes;

    @Scheduled(fixedDelayString = "${application.mail.queue.poll-interval-ms:5000}")
    public void processBatch() {
        for (int i = 0; i < batchSize; i++) {
            try {
                mailJobProcessor.processNextPendingJobIfAny();
            } catch (Exception e) {
                log.error("[MailWorker] batch slot failed: {}", e.getMessage());
            }
        }
    }

    @Scheduled(fixedDelayString = "${application.mail.queue.stuck-reset-interval-ms:600000}")
    @Transactional
    public void resetStuckProcessingJobs() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(stuckProcessingThresholdMinutes);
        mailJobRepository.resetStuckJobs(LocalDateTime.now(), threshold, STUCK_RECOVERY_MESSAGE);
        log.debug("[MailWorker] reset stuck PROCESSING jobs with updated_at before {}", threshold);
    }
}
