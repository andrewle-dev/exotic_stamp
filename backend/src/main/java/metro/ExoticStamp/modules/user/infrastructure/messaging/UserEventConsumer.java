package metro.ExoticStamp.modules.user.infrastructure.messaging;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventConsumer {

//    private final EmailService emailService;
//
//    @EventListener
//    @Async  // không block transaction createUser()
//    public void onUserCreated(UserCreatedEvent event) {
//        emailService.sendVerificationEmail(
//                event.getEmail(),
//                event.getUsername()
//        );
//    }
}

// ⚠ Bắt buộc thêm @EnableAsync vào Application.java:
// @SpringBootApplication
// @EnableAsync
// public