package metro.ExoticStamp.modules.auth.infrastructure.messaging;

import metro.ExoticStamp.infra.mail.MailService;
import metro.ExoticStamp.modules.user.domain.event.UserCreatedEvent;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.model.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AuthEventConsumerTest {

    @Mock private MailService mailService;
    @InjectMocks private AuthEventConsumer consumer;

    @Test
    void onUserCreated_sendsVerifyOtpWhenPresent() {
        User user = User.builder()
                .email("new@test.com")
                .username("newuser")
                .password("hash")
                .status(UserStatus.PENDING_VERIFIED)
                .build();
        user.setId(java.util.UUID.randomUUID());

        consumer.onUserCreated(new UserCreatedEvent(user, "123456"));

        verify(mailService).sendVerifyAccountOtp("new@test.com", "newuser", "123456");
    }

    @Test
    void onUserCreated_skipsWhenOtpBlank() {
        User user = User.builder()
                .email("new@test.com")
                .username("newuser")
                .password("hash")
                .status(UserStatus.PENDING_VERIFIED)
                .build();
        user.setId(java.util.UUID.randomUUID());

        consumer.onUserCreated(new UserCreatedEvent(user, "   "));

        verify(mailService, never()).sendVerifyAccountOtp(org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void onUserCreated_mailFailure_doesNotPropagate() {
        User user = User.builder()
                .email("fail@test.com")
                .username("failuser")
                .password("hash")
                .status(UserStatus.PENDING_VERIFIED)
                .build();
        user.setId(java.util.UUID.randomUUID());
        doThrow(new RuntimeException("smtp down")).when(mailService)
                .sendVerifyAccountOtp("fail@test.com", "failuser", "999999");

        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() ->
                consumer.onUserCreated(new UserCreatedEvent(user, "999999")));
    }
}
