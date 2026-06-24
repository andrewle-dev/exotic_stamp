package metro.ExoticStamp.modules.user.presentation.mapper;

import metro.ExoticStamp.modules.user.application.command.CreateUserCommand;
import metro.ExoticStamp.modules.user.application.command.UpdateUserCommand;
import metro.ExoticStamp.modules.user.application.view.UserView;
import metro.ExoticStamp.modules.user.presentation.dto.request.CreateUserRequest;
import metro.ExoticStamp.modules.user.presentation.dto.request.UpdateUserRequest;
import metro.ExoticStamp.modules.user.presentation.dto.response.UserResponse;

import java.util.UUID;

public final class UserPresentationMapper {

    private UserPresentationMapper() {
    }

    public static CreateUserCommand toCreateCommand(CreateUserRequest req) {
        return CreateUserCommand.builder()
                .firstname(req.getFirstname())
                .lastname(req.getLastname())
                .username(req.getUsername())
                .email(req.getEmail())
                .phoneNumber(req.getPhoneNumber())
                .password(req.getPassword())
                .dob(req.getDob())
                .gender(req.isGender())
                .build();
    }

    public static UpdateUserCommand toUpdateCommand(UUID id, UpdateUserRequest req) {
        return UpdateUserCommand.builder()
                .id(id)
                .firstname(req.getFirstname())
                .lastname(req.getLastname())
                .bio(req.getBio())
                .avatarUrl(req.getAvatarUrl())
                .gender(req.getGender())
                .dob(req.getDob())
                .build();
    }

    public static UserResponse toResponse(UserView view) {
        return UserResponse.builder()
                .id(view.getId())
                .firstname(view.getFirstname())
                .lastname(view.getLastname())
                .username(view.getUsername())
                .email(view.getEmail())
                .phoneNumber(view.getPhoneNumber())
                .dob(view.getDob())
                .gender(view.isGender())
                .bio(view.getBio())
                .avatarUrl(view.getAvatarUrl())
                .status(view.getStatus())
                .createdAt(view.getCreatedAt())
                .build();
    }
}
