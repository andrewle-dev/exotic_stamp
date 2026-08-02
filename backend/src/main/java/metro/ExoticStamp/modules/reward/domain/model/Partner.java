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

    @Column(name = "logo_url", length = 512)
    private String logoUrl;

    @Column(name = "banner_image_url", length = 512)
    private String bannerImageUrl;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Column(name = "contract_start_date")
    private LocalDate contractStartDate;

    @Column(name = "contract_end_date")
    private LocalDate contractEndDate;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    /**
     * Null contract dates are treated as open-ended.
     * When set, today must fall within [start, end] inclusive.
     */
    public boolean isWithinContractWindow(LocalDate today) {
        if (today == null) {
            return false;
        }
        if (contractStartDate != null && today.isBefore(contractStartDate)) {
            return false;
        }
        if (contractEndDate != null && today.isAfter(contractEndDate)) {
            return false;
        }
        return true;
    }

    /**
     * Eligibility for mobile Home promotional carousel:
     * active + non-blank banner + valid contract window.
     */
    public boolean isEligibleForPromotion(LocalDate today) {
        return active
                && bannerImageUrl != null
                && !bannerImageUrl.isBlank()
                && isWithinContractWindow(today);
    }

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
        if (bannerImageUrl != null) {
            bannerImageUrl = bannerImageUrl.trim();
            if (bannerImageUrl.isEmpty()) {
                bannerImageUrl = null;
            }
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
        if (logoUrl != null && logoUrl.length() > 512) {
            throw new IllegalArgumentException("Partner logoUrl length must be <= 512");
        }
        if (bannerImageUrl != null && bannerImageUrl.length() > 512) {
            throw new IllegalArgumentException("Partner bannerImageUrl length must be <= 512");
        }
        if (contactEmail != null && contactEmail.length() > 100) {
            throw new IllegalArgumentException("Partner contactEmail length must be <= 100");
        }
    }
}
