package metro.ExoticStamp.modules.user.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.user.application.port.UserAccountPort;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserAccountPortAdapter implements UserAccountPort {

    private final UserRepository userRepository;

    @Override
    public User save(User user) {
        return userRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByPhoneNumber(String phone) {
        return userRepository.existsByPhoneNumber(phone);
    }

    @Override
    public Optional<Long> findTokenVersionById(UUID id) {
        return userRepository.findTokenVersionById(id);
    }

    @Override
    public int incrementTokenVersionById(UUID id) {
        return userRepository.incrementTokenVersionById(id);
    }
}
