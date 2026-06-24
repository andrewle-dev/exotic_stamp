package metro.ExoticStamp.modules.user.application;

import metro.ExoticStamp.modules.user.application.command.CreateUserCommand;
import metro.ExoticStamp.modules.user.application.command.UpdateUserCommand;
import metro.ExoticStamp.modules.user.application.mapper.UserAppMapper;
import metro.ExoticStamp.modules.user.application.port.UserCachePort;
import metro.ExoticStamp.modules.user.application.view.UserView;
import metro.ExoticStamp.modules.user.domain.event.UserCreatedEvent;
import metro.ExoticStamp.modules.user.domain.exception.UserNotFoundException;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.model.UserStatus;
import metro.ExoticStamp.modules.user.domain.repository.UserRepository;
import metro.ExoticStamp.modules.user.domain.service.UserDomainService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserCommandService {

    private final UserRepository userRepository;
    private final UserDomainService domainService;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final UserCachePort cachePort;

    @Transactional
    public UserView createUser(CreateUserCommand cmd) {
        domainService.validateUniqueEmail(cmd.getEmail());
        domainService.validateUniqueUsername(cmd.getUsername());
        domainService.validateUniquePhone(cmd.getPhoneNumber());

        User user = User.builder()
                .firstname(cmd.getFirstname())
                .lastname(cmd.getLastname())
                .username(cmd.getUsername())
                .email(cmd.getEmail())
                .phoneNumber(cmd.getPhoneNumber())
                .password(passwordEncoder.encode(cmd.getPassword()))
                .dob(cmd.getDob())
                .gender(cmd.isGender())
                .status(UserStatus.ACTIVE)
                .verifiedAt(LocalDateTime.now().minusMinutes(1))
                .build();

        User saved = userRepository.save(user);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishEvent(UserCreatedEvent.from(saved));
            }
        });

        return UserAppMapper.toView(saved);
    }

    @Transactional
    public UserView updateUser(UpdateUserCommand cmd) {
        User user = userRepository.findById(cmd.getId())
                .orElseThrow(() -> new UserNotFoundException(cmd.getId()));

        if (cmd.getFirstname() != null) user.setFirstname(cmd.getFirstname());
        if (cmd.getLastname() != null) user.setLastname(cmd.getLastname());
        if (cmd.getBio() != null) user.setBio(cmd.getBio());
        if (cmd.getAvatarUrl() != null) user.setAvatarUrl(cmd.getAvatarUrl());
        if (cmd.getDob() != null) user.setDob(cmd.getDob());
        if (cmd.getGender() != null) user.setGender(cmd.getGender());

        UserView res = UserAppMapper.toView(userRepository.save(user));
        cachePort.evict(cmd.getId());
        return res;
    }

    @Transactional
    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) throw new UserNotFoundException(id);
        userRepository.deleteById(id);
        cachePort.evict(id);
    }
}
