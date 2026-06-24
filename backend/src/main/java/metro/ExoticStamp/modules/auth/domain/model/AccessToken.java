package metro.ExoticStamp.modules.auth.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "access_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken {

    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId; // NO @ManyToOne User

    @Column(unique = true, nullable = false)
    private String tokenHash; // SHA-256 of raw token

    @Column(nullable = false, length = 10)
    private String tokenType; // "REFRESH"

    @Column(length = 10)
    private String tokenPrefix; // "Bearer"

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime revokedAt; // nullable

    @Column(length = 40)
    private String revokedReason; // nullable

    @Column(nullable = false, length = 45)
    private String ipAddress;

    @Column(nullable = false)
    private String userAgent;

    @Column(nullable = false)
    private String deviceFingerprint;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean isRevoked() {
        return revokedAt != null;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !isRevoked() && !isExpired();
    }

    public static final String REASON_LOGOUT = "LOGOUT";
    public static final String REASON_LOGOUT_ALL = "LOGOUT_ALL";
    public static final String REASON_PASSWORD_RESET = "PASSWORD_RESET";
    public static final String REASON_REUSE_ATTACK = "REUSE_ATTACK";
    public static final String REASON_ROTATED = "ROTATED";
}

