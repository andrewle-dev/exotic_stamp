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
@Table(name = "stamp_designs")
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class StampDesign extends BaseEntity {

    @Column(name = "station_id")
    private UUID stationId;

    @Column(name = "campaign_id")
    private UUID campaignId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "image_url", nullable = false, length = 512)
    private String imageUrl;

    @Column(name = "preview_image_url", length = 512)
    private String previewImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StampRarity rarity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StampDesignStatus status;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Column(name = "animation_url", length = 255)
    private String animationUrl;

    @Column(name = "sound_url", length = 255)
    private String soundUrl;

    @Column(name = "is_limited", nullable = false)
    private boolean isLimited;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public boolean isDeleted() {
        return deletedAt != null;
    }

    public boolean isActiveDesign() {
        return status == StampDesignStatus.ACTIVE && !isDeleted();
    }
}
