package metro.ExoticStamp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.List;

@Data
@ConfigurationProperties(prefix = "application.rbac")
public class RbacProperties {

    /**
     * Role code used for last-admin protection (must match {@code roles.role}).
     */
    private String adminRoleCode = "ADMIN";

    /**
     * Role codes that are immutable for rename/delete/deactivation (includes {@link #adminRoleCode} by default in YAML).
     */
    private List<String> protectedSystemRoleCodes = new ArrayList<>(List.of("ADMIN"));

    private int maxRoleCodeLength = 64;

    private int maxPermissionCodeLength = 80;

    private int maxDescriptionLength = 500;
}
