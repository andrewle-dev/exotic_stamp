package metro.ExoticStamp.modules.auth.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import metro.ExoticStamp.modules.auth.domain.model.AuditLog;
import metro.ExoticStamp.modules.auth.domain.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void log(
            UUID userId,
            String tableName,
            String actionType,
            Object oldValue,
            Object newValue,
            String ipAddress
    ) {
        try {
            String oldJson = serializeQuietly(oldValue);
            String newJson = serializeQuietly(newValue);

            AuditLog auditLog = AuditLog.builder()
                    .id(UUID.randomUUID())
                    .userId(userId)
                    .tableName(tableName)
                    .actionType(actionType)
                    .actionTime(LocalDateTime.now())
                    .oldValue(oldJson)
                    .newValue(newJson)
                    .changedDate(null)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("[Auth] audit log failed err={}", e.getMessage());
        }
    }

    private String serializeQuietly(Object value) throws JsonProcessingException {
        if (value == null) {
            return null;
        }
        return objectMapper.writeValueAsString(value);
    }
}

