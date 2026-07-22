package metro.ExoticStamp.modules.auth.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import metro.ExoticStamp.modules.auth.domain.model.OtpType;
import org.hibernate.validator.constraints.time.DurationMin;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;

@ConfigurationProperties(prefix = "application.auth")
@Component
@Validated
@Getter
@Setter
public class AuthSecurityProperties {

    /**
     * Short window where a rotated refresh token may be presented again
     * (legitimate concurrent refresh / network retry). Outside this window,
     * reuse triggers family revocation.
     */
    @NotNull
    @DurationMin(seconds = 1)
    private Duration refreshReuseGrace = Duration.ofSeconds(30);

    @Valid
    @NotNull
    private Otp otp = new Otp();

    @Getter
    @Setter
    public static class Otp {

        @Min(4)
        private int length = 6;

        @Valid
        @NotNull
        private PurposeSettings forgotPassword = PurposeSettings.forgotPasswordDefaults();

        @Valid
        @NotNull
        private PurposeSettings emailVerify = PurposeSettings.emailVerifyDefaults();

        public PurposeSettings forType(OtpType type) {
            return type == OtpType.EMAIL_VERIFY ? emailVerify : forgotPassword;
        }
    }

    @Getter
    @Setter
    public static class PurposeSettings {

        @NotNull
        @DurationMin(seconds = 30)
        private Duration ttl = Duration.ofMinutes(5);

        @NotNull
        @DurationMin(seconds = 1)
        private Duration cooldownTtl = Duration.ofMinutes(2);

        @NotNull
        @DurationMin(minutes = 1)
        private Duration attemptsTtl = Duration.ofHours(1);

        @Min(1)
        private int maxAttempts = 5;

        static PurposeSettings forgotPasswordDefaults() {
            PurposeSettings settings = new PurposeSettings();
            settings.setTtl(Duration.ofMinutes(5));
            settings.setCooldownTtl(Duration.ofMinutes(2));
            settings.setAttemptsTtl(Duration.ofHours(1));
            settings.setMaxAttempts(5);
            return settings;
        }

        static PurposeSettings emailVerifyDefaults() {
            PurposeSettings settings = new PurposeSettings();
            settings.setTtl(Duration.ofMinutes(10));
            settings.setCooldownTtl(Duration.ofMinutes(2));
            settings.setAttemptsTtl(Duration.ofHours(1));
            settings.setMaxAttempts(5);
            return settings;
        }
    }
}
