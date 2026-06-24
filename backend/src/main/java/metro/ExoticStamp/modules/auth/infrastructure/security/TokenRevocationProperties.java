package metro.ExoticStamp.modules.auth.infrastructure.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties(prefix = "application.security.token-revocation")
@Validated
@Getter
@Setter
public class TokenRevocationProperties {

    /**
     * When true, access tokens remain valid if the DB token_version lookup fails (fail-open).
     * When false, DB errors cause revocation check to fail closed (reject token).
     */
    private boolean failOpenOnDbError = false;
}
