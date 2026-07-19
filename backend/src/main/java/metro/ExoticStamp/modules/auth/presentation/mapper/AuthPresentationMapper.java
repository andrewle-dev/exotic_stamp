package metro.ExoticStamp.modules.auth.presentation.mapper;

import metro.ExoticStamp.modules.auth.application.command.ChangePasswordCommand;
import metro.ExoticStamp.modules.auth.application.command.ForgotPasswordCommand;
import metro.ExoticStamp.modules.auth.application.command.LoginCommand;
import metro.ExoticStamp.modules.auth.application.command.RegisterCommand;
import metro.ExoticStamp.modules.auth.application.command.ResetPasswordCommand;
import metro.ExoticStamp.modules.auth.application.view.AuthUserView;
import metro.ExoticStamp.modules.auth.application.view.AuthView;
import metro.ExoticStamp.modules.auth.presentation.dto.request.ChangePasswordRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.ForgotPasswordRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.LoginRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.RegisterRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.ResetPasswordRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.response.AuthResponse;
import org.springframework.stereotype.Component;

@Component
public class AuthPresentationMapper {

    public LoginCommand toLoginCommand(LoginRequest req, String ip, String userAgent) {
        return LoginCommand.builder()
                .identifier(req.getIdentifier())
                .password(req.getPassword())
                .ipAddress(ip)
                .userAgent(userAgent)
                .deviceFingerprint(req.getDeviceFingerprint())
                .build();
    }

    public RegisterCommand toRegisterCommand(RegisterRequest req) {
        return RegisterCommand.builder()
                .firstname(req.getFirstname())
                .lastname(req.getLastname())
                .username(req.getUsername())
                .email(req.getEmail())
                .phoneNumber(req.getPhoneNumber())
                .password(req.getPassword())
                .build();
    }

    public ForgotPasswordCommand toForgotPasswordCommand(ForgotPasswordRequest req) {
        return ForgotPasswordCommand.builder()
                .email(req.getEmail())
                .build();
    }

    public ResetPasswordCommand toResetPasswordCommand(ResetPasswordRequest req) {
        return ResetPasswordCommand.builder()
                .email(req.getEmail())
                .otp(req.getOtp())
                .newPassword(req.getNewPassword())
                .build();
    }

    public ChangePasswordCommand toChangePasswordCommand(ChangePasswordRequest req, String ipAddress) {
        return ChangePasswordCommand.builder()
                .currentPassword(req.getCurrentPassword())
                .newPassword(req.getNewPassword())
                .confirmNewPassword(req.getConfirmNewPassword())
                .ipAddress(ipAddress)
                .build();
    }

    public AuthResponse toAuthResponse(AuthView view) {
        AuthResponse res = new AuthResponse();
        res.setAccessToken(view.accessToken());
        res.setRefreshToken(view.refreshToken());
        AuthUserView user = view.user();
        res.setUserInfo(new AuthResponse.UserInfo(
                user.id(),
                user.email(),
                user.username(),
                user.roles()
        ));
        return res;
    }
}
