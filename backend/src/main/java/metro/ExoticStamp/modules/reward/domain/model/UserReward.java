package metro.ExoticStamp.modules.reward.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "user_rewards")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserReward {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "campaign_id")
    private UUID campaignId;

    @Column(name = "reward_id")
    private UUID rewardId;

    @Column(name = "milestone_id", nullable = false)
    private UUID milestoneId;

    @Column(name = "voucher_pool_id")
    private UUID voucherPoolId;

    @Column(name = "issued_at", nullable = false)
    private LocalDateTime issuedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "redeemed_at")
    private LocalDateTime redeemedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RewardStatus status;

    @PrePersist
    @PreUpdate
    public void normalizeAndValidate() {
        if (userId == null) {
            throw new IllegalArgumentException("UserReward userId must not be null");
        }
        if (milestoneId == null) {
            throw new IllegalArgumentException("UserReward milestoneId must not be null");
        }
        if (status == null) {
            status = RewardStatus.ISSUED;
        }
        if (issuedAt == null) {
            issuedAt = LocalDateTime.now();
        }
    }
}
