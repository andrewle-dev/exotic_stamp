package metro.ExoticStamp.modules.auth.infrastructure.security;

import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.infrastructure.persistence.JpaUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final JpaUserRepository jpa;

    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Optional<User> byUuid = tryParseUuid(identifier).flatMap(jpa::findById);
        if (byUuid.isPresent()) {
            return byUuid.get();
        }

        return jpa.findByEmail(identifier)
                .or(() -> jpa.findByUsername(identifier))
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private static Optional<UUID> tryParseUuid(String value) {
        try {
            return Optional.of(UUID.fromString(value));
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}

