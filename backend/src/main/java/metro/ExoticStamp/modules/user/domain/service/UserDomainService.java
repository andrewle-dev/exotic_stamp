package metro.ExoticStamp.modules.user.domain.service;

import metro.ExoticStamp.common.exceptions.DomainRuleViolationException;
import metro.ExoticStamp.modules.user.domain.exception.UserFieldAlreadyTakenException;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.model.UserStatus;
import metro.ExoticStamp.modules.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

// @Component để Spring inject vào UserCommandService
// Chỉ chứa rule cần NHIỀU HƠN 1 entity hoặc cần query DB
@Component
@RequiredArgsConstructor
public class UserDomainService {

    private final UserRepository userRepository;

    public void validateUniqueEmail(String email) {
        if (userRepository.existsByEmail(email))
            throw new UserFieldAlreadyTakenException("email", email);
    }

    public void validateUniqueUsername(String username) {
        if (userRepository.existsByUsername(username))
            throw new UserFieldAlreadyTakenException("username", username);
    }

    public void validateUniquePhone(String phone) {
        if (userRepository.existsByPhoneNumber(phone))
            throw new UserFieldAlreadyTakenException("phone", phone);
    }

    // Status transition — BANNED không reactivate trực tiếp
    public void validateStatusTransition(UserStatus from, UserStatus to) {
        if (from == UserStatus.BANNED && to == UserStatus.ACTIVE)
            throw new DomainRuleViolationException("Cannot reactivate a banned user directly");
    }

    // Cross-field: OAuth2 user không có local password
    // Validate từng field đơn lẻ đã nằm trong User.java @PrePersist
    public void validateOauth2HasNoPassword(User user) {
        if (user.getOauth2provider() != null
                && user.getPassword() != null
                && !user.getPassword().isBlank())
            throw new DomainRuleViolationException("OAuth2 user must not have local password");
    }
}