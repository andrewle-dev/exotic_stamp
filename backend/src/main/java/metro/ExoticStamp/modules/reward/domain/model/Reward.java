package metro.ExoticStamp.modules.reward.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import metro.ExoticStamp.common.entity.BaseEntity;

import java.math.BigDecimal;
import java.util.UUID;

@Deprecated(since = "Stage 5", forRemoval = false)
@Data
@Entity
@Table(name = "rewards")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Reward extends BaseEntity {

    @Column(name = "milestone_id", nullable = false)
    private UUID milestoneId;

    @Column(name = "partner_id")
    private UUID partnerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 20)
    private RewardType rewardType;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "value_amount", precision = 10, scale = 2)
    private BigDecimal valueAmount;

    @Column(name = "expiry_days")
    private Integer expiryDays;

    @Column(name = "total_stock")
    private Integer totalStock;

    @Column(name = "issued_count", nullable = false)
    private Integer issuedCount;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    @PrePersist
    public void onPrePersist() {
        normalize();
        validate();
        if (issuedCount == null) {
            issuedCount = 0;
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        normalize();
        validate();
    }

    private void normalize() {
        if (name != null) {
            name = name.trim();
        }
        if (description != null) {
            description = description.trim();
        }
    }

    private void validate() {
        if (milestoneId == null) {
            throw new IllegalArgumentException("Reward milestoneId must not be null");
        }
        if (rewardType == null) {
            throw new IllegalArgumentException("Reward rewardType must not be null");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Reward name must not be blank");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Reward name length must be <= 100");
        }
        if (description != null && description.length() > 255) {
            throw new IllegalArgumentException("Reward description length must be <= 255");
        }
        if (issuedCount != null && issuedCount < 0) {
            throw new IllegalArgumentException("Reward issuedCount must be >= 0");
        }
        if (totalStock != null && totalStock < 0) {
            throw new IllegalArgumentException("Reward totalStock must be >= 0 when set");
        }
        if (expiryDays != null && expiryDays < 0) {
            throw new IllegalArgumentException("Reward expiryDays must be >= 0 when set");
        }
    }
}
