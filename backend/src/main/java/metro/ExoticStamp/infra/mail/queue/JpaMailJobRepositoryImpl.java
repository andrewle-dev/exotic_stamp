package metro.ExoticStamp.infra.mail.queue;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Repository
public class JpaMailJobRepositoryImpl implements JpaMailJobRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @SuppressWarnings("unchecked")
    public List<MailJob> findPendingJobsForUpdate(LocalDateTime now, int limit) {
        try {
            return entityManager.createNativeQuery(
                            """
                                    SELECT * FROM mail_jobs
                                    WHERE status = :status
                                    AND (next_retry_at IS NULL OR next_retry_at <= :now)
                                    ORDER BY created_at ASC
                                    LIMIT :limit
                                    FOR UPDATE SKIP LOCKED
                                    """,
                            MailJob.class)
                    .setParameter("status", MailJobStatus.PENDING.name())
                    .setParameter("now", now)
                    .setParameter("limit", limit)
                    .getResultList();
        } catch (Exception e) {
            log.warn("[MailQueue] findPendingJobsForUpdate failed: {}", e.getMessage());
            return Collections.emptyList();
        }
    }
}
