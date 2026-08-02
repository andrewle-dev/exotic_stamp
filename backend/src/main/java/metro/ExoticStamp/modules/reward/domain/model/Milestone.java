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

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "milestones")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Milestone extends BaseEntity {

    @Column(name = "line_id")
    private UUID lineId;

    @Column(name = "campaign_id")
    private UUID campaignId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(name = "stamps_required", nullable = false)
    private Integer stampsRequired;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "reward_type", nullable = false, length = 30)
    private RewardType rewardType;

    @Column(name = "reward_title", nullable = false, length = 100)
    private String rewardTitle;

    @Column(name = "reward_description", length = 255)
    private String rewardDescription;

    @Column(name = "reward_image_url", length = 512)
    private String rewardImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MilestoneStatus status;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    /** Legacy column kept in sync with {@link #status}. */
    @Column(name = "is_active", nullable = false)
    private boolean active;

    public boolean isArchived() {
        return status == MilestoneStatus.ARCHIVED;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isEvaluable() {
        return status == MilestoneStatus.ACTIVE && deletedAt == null;
    }

    @PrePersist
    @PreUpdate
    public void normalizeAndValidate() {
        if (code != null) {
            code = code.trim().toUpperCase();
        }
        if (name != null) {
            name = name.trim();
        }
        if (description != null) {
            description = description.trim();
        }
        if (rewardTitle != null) {
            rewardTitle = rewardTitle.trim();
        }
        if (rewardDescription != null) {
            rewardDescription = rewardDescription.trim();
        }
        if (status == null) {
            status = MilestoneStatus.DRAFT;
        }
        active = status == MilestoneStatus.ACTIVE;
        if (sortOrder == null) {
            sortOrder = 0;
        }
        if (stampsRequired == null || stampsRequired < 1) {
            throw new IllegalArgumentException("Milestone stampsRequired must be >= 1");
        }
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Milestone code must not be blank");
        }
        if (rewardType == null) {
            throw new IllegalArgumentException("Milestone rewardType must not be null");
        }
        if (rewardTitle == null || rewardTitle.isBlank()) {
            throw new IllegalArgumentException("Milestone rewardTitle must not be blank");
        }
        if (sortOrder < 0) {
            throw new IllegalArgumentException("Milestone sortOrder must be >= 0");
        }
    }
}
