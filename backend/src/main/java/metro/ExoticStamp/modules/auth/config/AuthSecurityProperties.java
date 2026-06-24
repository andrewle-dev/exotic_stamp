package metro.ExoticStamp.modules.auth.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
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

    @Valid
    @NotNull
    private Otp otp = new Otp();

    @Valid
    @NotNull
    private Verification verification = new Verification();

    @Getter
    @Setter
    public static class Otp {

        @Min(4)
        private int length = 6;

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
    }

    @Getter
    @Setter
    public static class Verification {

        @NotNull
        @DurationMin(minutes = 1)
        private Duration tokenTtl = Duration.ofMinutes(15);

        @NotNull
        @DurationMin(seconds = 1)
        private Duration resendCooldownTtl = Duration.ofMinutes(2);
    }
}
