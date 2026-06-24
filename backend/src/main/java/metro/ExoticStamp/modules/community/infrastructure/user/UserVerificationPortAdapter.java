package metro.ExoticStamp.modules.community.infrastructure.user;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.community.application.port.UserVerificationPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserVerificationPortAdapter implements UserVerificationPort {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public boolean isEmailVerified(UUID userId) {
        Boolean verified = jdbcTemplate.queryForObject(
                "SELECT verified_at IS NOT NULL FROM users WHERE id = ?",
                Boolean.class,
                userId
        );
        return Boolean.TRUE.equals(verified);
    }
}
