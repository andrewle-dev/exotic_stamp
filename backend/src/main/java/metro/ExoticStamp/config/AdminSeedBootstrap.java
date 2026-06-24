package metro.ExoticStamp.config;

import metro.ExoticStamp.modules.rbac.domain.model.Role;
import metro.ExoticStamp.modules.rbac.domain.model.RoleName;
import metro.ExoticStamp.modules.rbac.domain.model.RoleStatus;
import metro.ExoticStamp.modules.rbac.domain.model.UserRole;
import metro.ExoticStamp.modules.rbac.domain.repository.RoleRepository;
import metro.ExoticStamp.modules.rbac.domain.repository.UserRoleRepository;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.model.UserStatus;
import metro.ExoticStamp.modules.user.domain.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Dev/admin bootstrap — not loaded by {@code @WebMvcTest} slice (beans live here, not on {@code @SpringBootConfiguration}).
 */
@Configuration
@Profile("dev")
public class AdminSeedBootstrap {

    @Bean
    public CommandLineRunner adminSeedCommandLineRunner(
            UserRepository userRepository,
            RoleRepository roleRepository,
            UserRoleRepository userRoleRepository,
            PasswordEncoder passwordEncoder,
            @Value("${application.bootstrap.admin-password}") String adminPassword
    ) {
        return args -> {
            User adminUser = userRepository.findByUsername("admin")
                    .orElseGet(() -> userRepository.save(
                            User.builder()
                                    .firstname("Face Wash Fox")
                                    .lastname("IT")
                                    .username("admin")
                                    .email("admin@exoticstamp.local")
                                    .phoneNumber("+10000000000")
                                    .password(passwordEncoder.encode(adminPassword))
                                    .status(UserStatus.ACTIVE)
                                    .verifiedAt(LocalDateTime.now())
                                    .build()
                    ));

            Role adminRole = roleRepository.findByRoleName(RoleName.ADMIN)
                    .orElseGet(() -> roleRepository.save(
                            Role.builder()
                                    .role(RoleName.ADMIN.name())
                                    .status(RoleStatus.ACTIVE)
                                    .systemRole(true)
                                    .build()
                    ));

            UUID adminUserId = adminUser.getId();
            UUID adminRoleId = adminRole.getId();

            if (adminUserId != null && adminRoleId != null
                    && !userRoleRepository.existsByUserIdAndRoleId(adminUserId, adminRoleId)) {
                userRoleRepository.save(
                        UserRole.builder()
                                .userId(adminUserId)
                                .role(adminRole)
                                .build()
                );
            }
        };
    }
}
