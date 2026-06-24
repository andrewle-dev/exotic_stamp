package metro.ExoticStamp.infra.mail.queue;

import java.time.LocalDateTime;
import java.util.List;

public interface JpaMailJobRepositoryCustom {

    List<MailJob> findPendingJobsForUpdate(LocalDateTime now, int limit);
}
