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

import java.util.regex.Pattern;

@Data
@Entity
@Table(name = "lines")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Line extends BaseEntity {

    private static final Pattern HEX_COLOR = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    @Column(nullable = false, unique = true, length = 10)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(length = 500)
    private String description;

    @Column(name = "color", length = 7)
    private String colorHex;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "total_stations", nullable = false)
    private Integer totalStations;

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
        if (this.colorHex != null) {
            this.colorHex = this.colorHex.trim();
        }
    }

    public void validate() {
        if (this.code == null || this.code.isBlank()) {
            throw new IllegalArgumentException("Line code must not be blank");
        }
        if (this.code.length() > 10) {
            throw new IllegalArgumentException("Line code length must be <= 10");
        }
        if (this.name == null || this.name.isBlank()) {
            throw new IllegalArgumentException("Line name must not be blank");
        }
        if (this.name.length() > 100) {
            throw new IllegalArgumentException("Line name length must be <= 100");
        }
        if (this.colorHex != null && !this.colorHex.isBlank() && !HEX_COLOR.matcher(this.colorHex).matches()) {
            throw new IllegalArgumentException("Line colorHex must be HEX format #RRGGBB");
        }
        if (this.sortOrder == null || this.sortOrder < 0) {
            throw new IllegalArgumentException("Line sortOrder must be >= 0");
        }
        if (this.totalStations == null || this.totalStations < 0) {
            throw new IllegalArgumentException("Line totalStations must be >= 0");
        }
        if (this.status == null) {
            throw new IllegalArgumentException("Line status must not be null");
        }
    }

    public boolean isActive() {
        return MetroStatus.ACTIVE == status;
    }
}
