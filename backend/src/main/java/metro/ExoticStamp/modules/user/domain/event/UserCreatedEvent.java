package metro.ExoticStamp.modules.user.domain.event;

import metro.ExoticStamp.modules.user.domain.model.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserCreatedEvent {

    private final String userId;
    private final String email;
    private final String username;
    private final String verifyToken;
    private final LocalDateTime occurredAt;

    public UserCreatedEvent(User user, String verifyToken) {
        this.userId = user.getId().toString();
        this.email = user.getEmail();
        this.username = user.getUsername();
        this.verifyToken = verifyToken;
        this.occurredAt = LocalDateTime.now();
    }

    public static UserCreatedEvent from(User user) {
        return new UserCreatedEvent(user, null);
    }
}
