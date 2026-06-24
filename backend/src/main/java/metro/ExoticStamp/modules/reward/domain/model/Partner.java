package metro.ExoticStamp.modules.reward.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import metro.ExoticStamp.common.entity.BaseEntity;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "partners")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Partner extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Column(name = "is_active", nullable = false)
    private boolean active;

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
        if (name != null) {
            name = name.trim();
        }
        if (logoUrl != null) {
            logoUrl = logoUrl.trim();
        }
        if (contactEmail != null) {
            contactEmail = contactEmail.trim();
        }
    }

    private void validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Partner name must not be blank");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Partner name length must be <= 100");
        }
        if (logoUrl != null && logoUrl.length() > 255) {
            throw new IllegalArgumentException("Partner logoUrl length must be <= 255");
        }
        if (contactEmail != null && contactEmail.length() > 100) {
            throw new IllegalArgumentException("Partner contactEmail length must be <= 100");
        }
    }
}
