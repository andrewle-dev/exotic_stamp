package metro.ExoticStamp.modules.auth.infrastructure.persistence;

import metro.ExoticStamp.modules.auth.domain.model.AuditLog;
import metro.ExoticStamp.modules.auth.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuditLogRepositoryAdapter implements AuditLogRepository {

    private final JpaAuditLogRepository jpa;

    @Override
    public AuditLog save(AuditLog log) {
        return jpa.save(log);
    }

    @Override
    public List<AuditLog> findByUserId(UUID userId) {
        return jpa.findByUserId(userId);
    }
}

