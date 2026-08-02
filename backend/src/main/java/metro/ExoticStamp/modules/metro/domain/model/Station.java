package metro.ExoticStamp.modules.metro.domain.model;

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
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "stations")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Station extends BaseEntity {

    @Column(name = "line_id", nullable = false)
    private UUID lineId;

    @Column(nullable = false, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(length = 500)
    private String description;

    @Column(name = "historical_info")
    private String historicalInfo;

    @Column(length = 255)
    private String address;

    @Column(name = "image_url", length = 512)
    private String imageUrl;

    @Column(name = "stamp_preview_url", length = 512)
    private String stampPreviewUrl;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "zone_radius_meters")
    private Integer zoneRadiusMeters;

    @Column(name = "nfc_tag_id", unique = true, length = 100)
    private String nfcTagId;

    @Column(name = "qr_code_token", unique = true, length = 100)
    private String qrCodeValue;

    @Enumerated(EnumType.STRING)
    @Column(name = "scan_key_status", nullable = false, length = 20)
    private ScanKeyStatus scanKeyStatus;

    @Column(name = "last_qr_rotated_at")
    private LocalDateTime lastQrRotatedAt;

    @Column(name = "last_scan_key_updated_at")
    private LocalDateTime lastScanKeyUpdatedAt;

    @Column(name = "collector_count", nullable = false)
    private Integer collectorCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MetroStatus status;

    @PrePersist
    public void onPrePersist() {
        normalize();
        validate();
    }

    @PreUpdate
    public void onPreUpdate() {
        normalize();
        validate();
    }

    private void normalize() {
        if (this.code != null) {
            this.code = this.code.trim().toUpperCase();
        }
        if (this.name != null) {
            this.name = this.name.trim();
        }
        if (this.displayName != null) {
            this.displayName = this.displayName.trim();
        }
        if (this.description != null) {
            this.description = this.description.trim();
        }
        if (this.historicalInfo != null) {
            this.historicalInfo = this.historicalInfo.trim();
        }
        if (this.address != null) {
            this.address = this.address.trim();
        }
        if (this.imageUrl != null) {
            this.imageUrl = this.imageUrl.trim();
        }
        if (this.stampPreviewUrl != null) {
            this.stampPreviewUrl = this.stampPreviewUrl.trim();
        }
        if (this.nfcTagId != null) {
            this.nfcTagId = this.nfcTagId.trim();
        }
        if (this.qrCodeValue != null) {
            this.qrCodeValue = this.qrCodeValue.trim();
        }
    }

    public void validate() {
        if (this.lineId == null) {
            throw new IllegalArgumentException("Station lineId must not be null");
        }
        if (this.code == null || this.code.isBlank()) {
            throw new IllegalArgumentException("Station code must not be blank");
        }
        if (this.code.length() > 20) {
            throw new IllegalArgumentException("Station code length must be <= 20");
        }
        if (this.name == null || this.name.isBlank()) {
            throw new IllegalArgumentException("Station name must not be blank");
        }
        if (this.name.length() > 100) {
            throw new IllegalArgumentException("Station name length must be <= 100");
        }
        if (this.sortOrder == null || this.sortOrder < 0) {
            throw new IllegalArgumentException("Station sortOrder must be >= 0");
        }
        if (this.description != null && this.description.length() > 500) {
            throw new IllegalArgumentException("Station description length must be <= 500");
        }
        if (this.latitude != null
                && (this.latitude.compareTo(BigDecimal.valueOf(-90)) < 0
                || this.latitude.compareTo(BigDecimal.valueOf(90)) > 0)) {
            throw new IllegalArgumentException("Station latitude must be between -90 and 90");
        }
        if (this.longitude != null
                && (this.longitude.compareTo(BigDecimal.valueOf(-180)) < 0
                || this.longitude.compareTo(BigDecimal.valueOf(180)) > 0)) {
            throw new IllegalArgumentException("Station longitude must be between -180 and 180");
        }
        if (this.zoneRadiusMeters != null && (this.zoneRadiusMeters < 20 || this.zoneRadiusMeters > 1000)) {
            throw new IllegalArgumentException("Station zoneRadiusMeters must be between 20 and 1000");
        }
        if (this.nfcTagId != null && this.nfcTagId.length() > 100) {
            throw new IllegalArgumentException("Station nfcTagId length must be <= 100");
        }
        if (this.qrCodeValue != null && this.qrCodeValue.length() > 100) {
            throw new IllegalArgumentException("Station qrCodeValue length must be <= 100");
        }
        if (this.collectorCount == null || this.collectorCount < 0) {
            throw new IllegalArgumentException("Station collectorCount must be >= 0");
        }
        if (this.status == null) {
            throw new IllegalArgumentException("Station status must not be null");
        }
        if (this.scanKeyStatus == null) {
            throw new IllegalArgumentException("Station scanKeyStatus must not be null");
        }
    }

    public boolean isActive() {
        return MetroStatus.ACTIVE == status;
    }
}
