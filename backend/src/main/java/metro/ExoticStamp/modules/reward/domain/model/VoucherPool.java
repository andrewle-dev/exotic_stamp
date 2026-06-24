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
@Table(name = "voucher_pool")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherPool {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "milestone_id")
    private UUID milestoneId;

    @Column(name = "reward_id")
    private UUID rewardId;

    @Column(nullable = false, length = 50)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VoucherPoolStatus status;

    @Column(name = "assigned_user_id")
    private UUID assignedUserId;

    @Column(name = "assigned_user_reward_id")
    private UUID assignedUserRewardId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /** Legacy column; kept in sync with status for backward reads. */
    @Column(name = "is_redeemed", nullable = false)
    private boolean redeemed;

    @PrePersist
    @PreUpdate
    public void normalizeAndValidate() {
        if (code != null) {
            code = code.trim();
        }
        if (status == null) {
            status = VoucherPoolStatus.AVAILABLE;
        }
        redeemed = status == VoucherPoolStatus.CLAIMED;
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("VoucherPool code must not be blank");
        }
        if (milestoneId == null && rewardId == null) {
            throw new IllegalArgumentException("VoucherPool milestoneId or rewardId required");
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        updatedAt = LocalDateTime.now();
    }
}
