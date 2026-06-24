package metro.ExoticStamp.modules.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import metro.ExoticStamp.modules.auth.application.AuthCommandService;
import metro.ExoticStamp.modules.auth.application.command.ForgotPasswordCommand;
import metro.ExoticStamp.modules.auth.application.command.LoginCommand;
import metro.ExoticStamp.modules.auth.application.command.RefreshTokenCommand;
import metro.ExoticStamp.modules.auth.application.command.ResendOtpCommand;
import metro.ExoticStamp.modules.auth.application.command.ResetPasswordCommand;
import metro.ExoticStamp.modules.auth.application.command.RegisterCommand;
import metro.ExoticStamp.modules.auth.application.command.ResendVerificationCommand;
import metro.ExoticStamp.modules.auth.application.command.VerifyTokenCommand;
import metro.ExoticStamp.modules.auth.application.port.TokenTtlPort;
import metro.ExoticStamp.modules.auth.presentation.mapper.AuthPresentationMapper;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.auth.presentation.dto.request.ForgotPasswordRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.LoginRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.RegisterRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.ResetPasswordRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.ResendOtpRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.ResendVerificationRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.VerifyTokenRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.response.AuthResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    private static final String REFRESH_TOKEN_COOKIE_PATH = "/api/v1/auth/refresh";

    private static final int CLEAR_COOKIE_MAX_AGE_SECONDS = 0;

    private final AuthCommandService commandService;
    private final AuthPresentationMapper presentationMapper;
    private final TokenTtlPort tokenTtlPort;

    @PostMapping("/login")
    @Operation(summary = "Login and issue access token")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getRemoteAddr();

        LoginCommand cmd = presentationMapper.toLoginCommand(req, ip, userAgent);
        AuthResponse res = presentationMapper.toAuthResponse(commandService.login(cmd));

        setRefreshTokenCookie(
                response,
                res.getRefreshToken(),
                request.isSecure()
        );
        res.clearRefreshToken();
        return ResponseEntity.ok(res);
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new account")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest req
    ) {
        RegisterCommand cmd = presentationMapper.toRegisterCommand(req);
        commandService.register(cmd);
        return ResponseEntity.ok("Registered successfully! Please check your email for verification.");
    }

    @PostMapping("/verify-email")
    @Operation(summary = "Verify email using verification token")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyTokenRequest req) {
        commandService.verifyEmail(new VerifyTokenCommand(req.getToken()));
        return ResponseEntity.ok(ApiResponse.ok("Email verified successfully.", null));
    }

    @PostMapping("/resend-verification")
    @Operation(summary = "Resend verification email")
    public ResponseEntity<ApiResponse<Void>> resendVerification(
            @Valid @RequestBody ResendVerificationRequest req) {
        commandService.resendVerification(new ResendVerificationCommand(req.getEmail()));
        return ResponseEntity.ok(
                ApiResponse.ok("If the account exists and is eligible, a verification email has been sent.", null));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Start forgot-password flow")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest req) {
        ForgotPasswordCommand cmd = presentationMapper.toForgotPasswordCommand(req);
        commandService.forgotPassword(cmd);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/resend-otp")
    @Operation(summary = "Resend forgot-password OTP")
    public ResponseEntity<ApiResponse<Void>> resendOtp(
            @Valid @RequestBody ResendOtpRequest req
    ) {
        commandService.resendForgotPasswordOtp(new ResendOtpCommand(req.getEmail()));
        return ResponseEntity.ok(
                ApiResponse.ok("If the email exists, a new OTP has been sent.", null));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password with OTP")
    public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest req) {
        ResetPasswordCommand cmd = presentationMapper.toResetPasswordCommand(req);
        commandService.resetPassword(cmd);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh cookie")
    public ResponseEntity<AuthResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = readCookieValue(request, REFRESH_TOKEN_COOKIE_NAME)
                .orElse(null);

        AuthResponse res = presentationMapper.toAuthResponse(commandService.refresh(
                new RefreshTokenCommand(refreshToken)
        ));

        setRefreshTokenCookie(
                response,
                res.getRefreshToken(),
                request.isSecure()
        );
        res.clearRefreshToken();
        return ResponseEntity.ok(res);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout current session", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal UserDetails principal,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        UUID userId = commandService.resolveUserId(principal);

        Optional<String> accessJti = commandService.parseOptionalAccessJti(
                request.getHeader("Authorization"),
                userId
        );
        Optional<String> refreshToken = readCookieValue(request, REFRESH_TOKEN_COOKIE_NAME);

        commandService.logout(userId, refreshToken, accessJti);

        clearRefreshTokenCookie(response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout all sessions", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> logoutAll(
            @AuthenticationPrincipal UserDetails principal,
            HttpServletResponse response
    ) {
        UUID userId = commandService.resolveUserId(principal);
        commandService.logoutAll(userId);
        clearRefreshTokenCookie(response);
        return ResponseEntity.ok().build();
    }

    private void setRefreshTokenCookie(
            HttpServletResponse response,
            String refreshToken,
            boolean secure
    ) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath(REFRESH_TOKEN_COOKIE_PATH);
        cookie.setMaxAge(refreshTokenCookieMaxAgeSeconds());
        response.addCookie(cookie);
    }

    private void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setPath(REFRESH_TOKEN_COOKIE_PATH);
        cookie.setMaxAge(CLEAR_COOKIE_MAX_AGE_SECONDS);
        response.addCookie(cookie);
    }

    private int refreshTokenCookieMaxAgeSeconds() {
        long seconds = Math.max(0L, tokenTtlPort.getRefreshTokenTtl().getSeconds());
        return seconds > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) seconds;
    }

    private Optional<String> readCookieValue(
            HttpServletRequest request,
            String cookieName
    ) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }
        for (Cookie cookie : cookies) {
            if (Objects.equals(cookie.getName(), cookieName)) {
                return Optional.ofNullable(cookie.getValue());
            }
        }
        return Optional.empty();
    }
}
