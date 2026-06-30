package metro.ExoticStamp.modules.auth.infrastructure.messaging;

import metro.ExoticStamp.infra.mail.MailService;
import metro.ExoticStamp.modules.user.domain.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthEventConsumer {

    private final MailService mailService;

    @EventListener
    @Async
    public void onUserCreated(UserCreatedEvent event) {
        try {
            if (event.getVerifyOtp() == null || event.getVerifyOtp().isBlank()) {
                return;
            }
            mailService.sendVerifyAccountOtp(
                    event.getEmail(),
                    event.getUsername(),
                    event.getVerifyOtp()
            );
            log.info("[AuthEvent] Verify account OTP sent to {}", event.getEmail());
        } catch (Exception e) {
            log.error("[AuthEvent] onUserCreated failed for {}: {}", event.getEmail(), e.getMessage());
        }
    }
}
