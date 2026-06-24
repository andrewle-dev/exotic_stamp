package metro.ExoticStamp.modules.auth.domain.repository;

import metro.ExoticStamp.modules.auth.domain.model.AuditLog;

import java.util.List;
import java.util.UUID;

public interface AuditLogRepository {
    AuditLog save(AuditLog log);

    List<AuditLog> findByUserId(UUID userId);
}

