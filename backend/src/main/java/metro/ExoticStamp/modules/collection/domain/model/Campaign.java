package metro.ExoticStamp.modules.collection.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import metro.ExoticStamp.common.entity.BaseEntity;

import java.time.LocalDateTime;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "campaigns")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Campaign extends BaseEntity {

    @Column(name = "partner_id")
    private UUID partnerId;

    /**
     * Metro line id (UUID) stored as a scalar to avoid cross-module JPA relationships.
     * Retained for default-per-line bootstrap and legacy collect resolution.
     */
    @Column(name = "line_id")
    private UUID lineId;

    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "campaign_type", nullable = false, length = 20)
    private CampaignType campaignType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignStatus status;

    @Column(name = "banner_image_url", length = 255)
    private String bannerImageUrl;

    @Column(name = "thumbnail_image_url", length = 255)
    private String thumbnailImageUrl;

    @Column(nullable = false)
    private int priority;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isArchived() {
        return status == CampaignStatus.ARCHIVED;
    }

    public boolean isActiveForCollection() {
        return status == CampaignStatus.ACTIVE && !isDeleted();
    }
}
