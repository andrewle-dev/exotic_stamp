package metro.ExoticStamp.modules.user.domain.model;

import metro.ExoticStamp.common.entity.BaseEntity;
import metro.ExoticStamp.common.security.AuthenticatedUser;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@EqualsAndHashCode(callSuper = true)
@Data
@Entity
@Table(name = "users")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User extends BaseEntity implements UserDetails, AuthenticatedUser {

    @Column(length = 50)
    private String firstname;

    @Column(length = 50)
    private String lastname;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false, length = 255)
    private String email;

    @Column(unique = true, nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @Column()
    private LocalDate dob;

    @Column()
    private boolean gender;

    @Column(length = 100)
    private String bio;

    @Column()
    private String avatarUrl;

    @Column()
    private String oauth2provider;

    @Column()
    @Enumerated(EnumType.STRING) // Hibernate'll store ordinal (1,2,3,...) - Data inconsistency when add/update order of enum so use @Enumerate
    private UserStatus status;

    @Column()
    private LocalDateTime verifiedAt;

    @Column()
    private LocalDateTime passwordUpdateAt;

    /**
     * Bumped on password reset, logout-all, and refresh-token reuse detection to invalidate access JWTs server-side.
     */
    @Builder.Default
    @Column(name = "token_version", nullable = false)
    private long tokenVersion = 0L;

    /* === VALIDATE USER INFO === */
    // Check Email (use REGEX) RFC 5322 Standard
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    // Check allowed provider in application
    private static final Set<String> ALLOWED_PROVIDERS = Set.of("google", "facebook", "github", "apple");

    /* ============================================================
     * INDIVIDUAL FIELD VALIDATORS
     * Validate for each part (example: update one field)
     * ============================================================ */
    public void validateEmail() {
        if (this.email == null || !EMAIL_PATTERN.matcher(this.email.trim()).matches()) {
            throw new IllegalArgumentException("Invalid email address");
        }
    }

    public void validateUsername() {
        String userTrimmed = this.username.trim();
        if (!userTrimmed.matches("^[A-Za-z0-9_-]+$") || userTrimmed.length() < 3) {
            throw new IllegalArgumentException("Username must be at least 3 characters and contain only letters, numbers, _ or -");
        }
    }

    public void validatePhoneNumber() {
        // If we expand internationally later, we can replace it with libphonenumber (Library)
        if (this.phoneNumber == null || !this.phoneNumber.matches("^\\+?[0-9]{9,15}$")) {
            throw new IllegalArgumentException("Invalid phone number format");
        }
    }

    public void validatePassword() {
        if (this.oauth2provider != null) return;

        if (this.password == null || this.password.isBlank()) {
            throw new IllegalArgumentException("Password must not be blank");
        }
        if (this.password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters");
        }
    }

    public void validateDob() {
        if (this.dob == null) return;

        if (this.dob.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Date of birth must not be in the future");
        }
        // Fix: Check the future first, then the age — avoid negative period values.
        if (Period.between(this.dob, LocalDate.now()).getYears() < 6) {
            throw new IllegalArgumentException("User must be at least 6 years old");
        }
    }

    public void validateBio() {
        // Fix: guard null first
        if (this.bio != null && this.bio.length() > 100) {
            throw new IllegalArgumentException("Bio must not exceed 100 characters");
        }
    }

    public void validateName() {
        if (this.firstname != null && this.firstname.length() > 50) {
            throw new IllegalArgumentException("Firstname must not exceed 50 characters");
        }
        if (this.lastname != null && this.lastname.length() > 50) {
            throw new IllegalArgumentException("Lastname must not exceed 50 characters");
        }
    }

    public void validateOauth2Provider() {
        if (this.oauth2provider != null
                && !ALLOWED_PROVIDERS.contains(this.oauth2provider.toLowerCase())) {
            throw new IllegalArgumentException(
                    "Unsupported OAuth2 provider: " + this.oauth2provider
            );
        }
    }

    public void validateStatus() {
        if (this.status == UserStatus.ACTIVE && this.verifiedAt == null) {
            throw new IllegalArgumentException("Active user must have a verification date");
        }
    }

    public void validatePasswordUpdateAt() {
        if (this.passwordUpdateAt != null && this.passwordUpdateAt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Password update date must not be in the future");
        }
    }

    /* ============================================================
     * FULL VALIDATION — call all field validators
     * ============================================================ */
    public void validate() {
        validateEmail();
        validateUsername();
        validatePhoneNumber();
        validatePassword();
        validateDob();
        validateBio();
        validateName();
        validateOauth2Provider();
        validateStatus();
        validatePasswordUpdateAt();
    }

    /* ============================================================
     * JPA HOOKS — automatically run before store in DB
     * ============================================================ */
    @PrePersist
    public void onPrePersist() {
        normalize();   // clean data first
        validate();    // then validate
    }

    @PreUpdate
    public void onPreUpdate() {
        normalize();
        validate();
    }

    /* ============================================================
     * NORMALIZE — normalize data before store in DB
     * ============================================================ */
    private void normalize() {
        if (this.email != null)         this.email         = this.email.trim().toLowerCase();
        if (this.username != null)      this.username      = this.username.trim();
        if (this.phoneNumber != null)   this.phoneNumber   = this.phoneNumber.trim();
        if (this.firstname != null)     this.firstname     = this.firstname.trim();
        if (this.lastname != null)      this.lastname      = this.lastname.trim();
        if (this.bio != null)           this.bio           = this.bio.trim();
        if (this.oauth2provider != null) this.oauth2provider = this.oauth2provider.trim();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of();
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public java.util.UUID getUserId() {
        return getId();
    }
}
