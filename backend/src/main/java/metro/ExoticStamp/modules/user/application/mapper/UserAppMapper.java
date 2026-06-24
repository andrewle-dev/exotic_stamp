package metro.ExoticStamp.modules.user.application.mapper;

import metro.ExoticStamp.modules.user.application.view.UserView;
import metro.ExoticStamp.modules.user.domain.model.User;

public final class UserAppMapper {

    private UserAppMapper() {
    }

    public static UserView toView(User user) {
        return UserView.builder()
                .id(user.getId())
                .firstname(user.getFirstname())
                .lastname(user.getLastname())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .dob(user.getDob())
                .gender(user.isGender())
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .status(user.getStatus() != null ? user.getStatus().name() : null)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
