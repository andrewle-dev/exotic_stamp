package metro.ExoticStamp.infra.mail.queue;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface JpaMailJobRepository extends JpaRepository<MailJob, UUID>, JpaMailJobRepositoryCustom {

    boolean existsByDedupKey(String dedupKey);

    Optional<MailJob> findByDedupKey(String dedupKey);

    @Modifying
    @Query(value = """
            UPDATE mail_jobs SET status = 'FAILED',
            last_error = :lastError,
            updated_at = :now
            WHERE status = 'PROCESSING'
            AND updated_at < :stuckThreshold
            """, nativeQuery = true)
    void resetStuckJobs(
            @Param("now") LocalDateTime now,
            @Param("stuckThreshold") LocalDateTime stuckThreshold,
            @Param("lastError") String lastError
    );
}
