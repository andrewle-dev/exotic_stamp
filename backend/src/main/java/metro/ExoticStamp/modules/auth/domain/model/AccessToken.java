package metro.ExoticStamp.modules.auth.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Refresh-session row (tokenType=REFRESH). {@code id} is the session id.
 * Raw refresh tokens are never persisted — only {@link #tokenHash}.
 */
@Entity
@Table(name = "access_tokens")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken {

    /** Session id. */
    @Id
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(unique = true, nullable = false)
    private String tokenHash;

    @Column(nullable = false, length = 10)
    private String tokenType;

    @Column(length = 10)
    private String tokenPrefix;

    @Column(name = "token_family_id", nullable = false)
    private UUID tokenFamilyId;

    @Column(name = "parent_token_id")
    private UUID parentTokenId;

    @Column(name = "replaced_by_token_id")
    private UUID replacedByTokenId;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column
    private LocalDateTime revokedAt;

    @Column(length = 40)
    private String revokedReason;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Column(nullable = false, length = 45)
    private String ipAddress;

    @Column(nullable = false, length = 1000)
    private String userAgent;

    /** App-generated or derived device id (metadata only). */
    @Column(nullable = false)
    private String deviceFingerprint;

    @Column(name = "client_platform", length = 20)
    private String clientPlatform;

    @Column(name = "user_agent_hash", length = 64)
    private String userAgentHash;

    @Column(name = "ip_hash", length = 64)
    private String ipHash;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Long version = 0L;

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
    public static final String REASON_PASSWORD_CHANGED = "PASSWORD_CHANGED";
    public static final String REASON_REUSE_ATTACK = "REUSE_ATTACK";
    public static final String REASON_ROTATED = "ROTATED";
    public static final String REASON_FAMILY_REUSE = "FAMILY_REUSE";
}
