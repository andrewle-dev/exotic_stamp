package metro.ExoticStamp.modules.community.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "referrals")
public class Referral {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "referrer_user_id", nullable = false)
    private UUID referrerUserId;

    @Column(name = "referred_user_id", nullable = false, unique = true)
    private UUID referredUserId;

    @Column(name = "referral_code_id", nullable = false)
    private UUID referralCodeId;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReferralStatus status = ReferralStatus.PENDING;

    @Column(name = "referred_at", nullable = false, updatable = false)
    private LocalDateTime referredAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "reward_issued_at")
    private LocalDateTime rewardIssuedAt;

    @PrePersist
    void onCreate() {
        if (referredAt == null) {
            referredAt = LocalDateTime.now();
        }
        if (status == null) {
            status = ReferralStatus.PENDING;
        }
    }
}
