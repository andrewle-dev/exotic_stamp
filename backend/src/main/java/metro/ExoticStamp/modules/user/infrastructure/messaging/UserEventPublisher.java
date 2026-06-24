package metro.ExoticStamp.modules.user.infrastructure.messaging;

import metro.ExoticStamp.modules.user.domain.event.UserCreatedEvent;
import metro.ExoticStamp.modules.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishUserCreated(User user) {
        publisher.publishEvent(UserCreatedEvent.from(user));
    }

    // Mở rộng: publishUserBanned, publishPasswordChanged...
}