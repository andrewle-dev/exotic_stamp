package metro.ExoticStamp.infra.storage.asset;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import metro.ExoticStamp.infra.storage.StorageVisibility;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "stored_assets")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoredAsset {

    @Id
    private UUID id;

    @Column(nullable = false, length = 32)
    private String provider;

    @Column(name = "object_key", nullable = false, length = 512, unique = true)
    private String objectKey;

    @Column(name = "content_type", length = 128)
    private String contentType;

    @Column(name = "byte_size")
    private Long byteSize;

    @Column(length = 128)
    private String checksum;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StorageVisibility visibility;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private StoredAssetStatus status;

    @Column(name = "entity_type", length = 64)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @Column(name = "public_url", length = 512)
    private String publicUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "orphaned_at")
    private LocalDateTime orphanedAt;

    @Column(name = "delete_after")
    private LocalDateTime deleteAfter;

    @PrePersist
    void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
