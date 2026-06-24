package metro.ExoticStamp.modules.user.application;

import metro.ExoticStamp.modules.user.application.command.UpdateUserCommand;
import metro.ExoticStamp.modules.user.application.port.UserCachePort;
import metro.ExoticStamp.modules.user.domain.exception.UserFieldAlreadyTakenException;
import metro.ExoticStamp.modules.user.domain.exception.UserNotFoundException;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.model.UserStatus;
import metro.ExoticStamp.modules.user.domain.repository.UserRepository;
import metro.ExoticStamp.modules.user.domain.service.UserDomainService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserCommandServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock private UserRepository userRepository;
    @Mock private UserDomainService domainService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private UserCachePort cachePort;

    @InjectMocks
    private UserCommandService userCommandService;

    @Test
    void updateUser_success_evictsCache() {
        User user = User.builder()
                .username("u1")
                .email("u1@test.com")
                .phoneNumber("+10000000003")
                .password("hash")
                .status(UserStatus.ACTIVE)
                .build();
        user.setId(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        userCommandService.updateUser(UpdateUserCommand.builder()
                .id(USER_ID)
                .firstname("New")
                .build());

        assertEquals("New", user.getFirstname());
        verify(cachePort).evict(USER_ID);
    }

    @Test
    void updateUser_notFound_throws() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());
        assertThrows(UserNotFoundException.class, () -> userCommandService.updateUser(
                UpdateUserCommand.builder().id(USER_ID).firstname("X").build()));
    }
}
