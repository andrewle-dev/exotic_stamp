package metro.ExoticStamp.modules.metro.domain.model;

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
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "station_scan_keys")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StationScanKey {

    public static final String DEFAULT_PAYLOAD_SCHEME = "metrostamp://scan";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "station_id", nullable = false)
    private UUID stationId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scan_type", nullable = false, length = 30)
    private ScanType scanType;

    @Column(name = "key_hash", nullable = false, unique = true, length = 128)
    private String keyHash;

    @Column(name = "key_prefix", nullable = false, length = 32)
    private String keyPrefix;

    @Column(name = "payload_scheme", nullable = false, length = 50)
    private String payloadScheme;

    @Column(length = 100)
    private String label;

    @Column(name = "placement_note", length = 255)
    private String placementNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ScanKeyStatus status;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "replaced_by_id")
    private UUID replacedById;

    @Column(name = "last_seen_at")
    private LocalDateTime lastSeenAt;

    @Column(name = "last_install_verified_at")
    private LocalDateTime lastInstallVerifiedAt;

    @Column(name = "installed_latitude")
    private Double installedLatitude;

    @Column(name = "installed_longitude")
    private Double installedLongitude;

    @Column(name = "installed_accuracy_meters")
    private Double installedAccuracyMeters;

    @Column(name = "installed_device_platform", length = 30)
    private String installedDevicePlatform;

    @Column(name = "installed_app_version", length = 50)
    private String installedAppVersion;

    @Column(name = "installed_by")
    private UUID installedBy;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    @PrePersist
    public void onPrePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (payloadScheme == null || payloadScheme.isBlank()) {
            payloadScheme = DEFAULT_PAYLOAD_SCHEME;
        }
        if (version == null) {
            version = 0L;
        }
        normalize();
    }

    @PreUpdate
    public void onPreUpdate() {
        updatedAt = LocalDateTime.now();
        normalize();
    }

    private void normalize() {
        if (label != null) {
            label = label.trim();
            if (label.isEmpty()) {
                label = null;
            }
        }
        if (placementNote != null) {
            placementNote = placementNote.trim();
            if (placementNote.isEmpty()) {
                placementNote = null;
            }
        }
        if (installedDevicePlatform != null) {
            installedDevicePlatform = installedDevicePlatform.trim();
        }
        if (installedAppVersion != null) {
            installedAppVersion = installedAppVersion.trim();
        }
    }

    public boolean isActive() {
        return status == ScanKeyStatus.ACTIVE;
    }

    public boolean canActivate() {
        return status == ScanKeyStatus.DRAFT || status == ScanKeyStatus.INACTIVE;
    }
}
