package metro.ExoticStamp.modules.auth.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import metro.ExoticStamp.modules.auth.application.AuthCommandService;
import metro.ExoticStamp.modules.auth.application.command.ChangePasswordCommand;
import metro.ExoticStamp.modules.auth.application.command.ForgotPasswordCommand;
import metro.ExoticStamp.modules.auth.application.command.LoginCommand;
import metro.ExoticStamp.modules.auth.application.command.RefreshTokenCommand;
import metro.ExoticStamp.modules.auth.application.command.ResendOtpCommand;
import metro.ExoticStamp.modules.auth.application.command.ResetPasswordCommand;
import metro.ExoticStamp.modules.auth.application.command.RegisterCommand;
import metro.ExoticStamp.modules.auth.application.command.ResendVerificationCommand;
import metro.ExoticStamp.modules.auth.application.command.VerifyAccountCommand;
import metro.ExoticStamp.modules.auth.domain.model.AuthTransport;
import metro.ExoticStamp.modules.auth.domain.model.ClientPlatform;
import metro.ExoticStamp.modules.auth.presentation.mapper.AuthPresentationMapper;
import metro.ExoticStamp.modules.auth.presentation.support.AuthTransportResolver;
import metro.ExoticStamp.modules.auth.presentation.support.RefreshCookieSupport;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.auth.presentation.dto.request.ChangePasswordRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.ForgotPasswordRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.LoginRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.RefreshTokenRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.RegisterRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.ResetPasswordRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.ResendOtpRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.ResendVerificationRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.VerifyAccountRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.response.AuthResponse;
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

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth")
public class AuthController {

    private final AuthCommandService commandService;
    private final AuthPresentationMapper presentationMapper;
    private final RefreshCookieSupport refreshCookieSupport;

    @PostMapping("/login")
    @Operation(summary = "Login and issue access token")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest req,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        AuthTransport transport = AuthTransportResolver.transportFromRequest(request);
        String userAgent = request.getHeader("User-Agent");
        String ip = request.getRemoteAddr();

        LoginCommand cmd = presentationMapper.toLoginCommand(req, ip, userAgent);
        cmd.setTransport(transport);
        cmd.setClientPlatform(ClientPlatform.fromTransport(transport));

        AuthResponse res = presentationMapper.toAuthResponse(commandService.login(cmd));
        applyTransportResponse(transport, request, response, res);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new account")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest req
    ) {
        RegisterCommand cmd = presentationMapper.toRegisterCommand(req);
        commandService.register(cmd);
        return ResponseEntity.ok("Registered successfully! Please check your email for your verification code.");
    }

    @PostMapping("/verify-account")
    @Operation(summary = "Verify account with email OTP")
    public ResponseEntity<ApiResponse<Void>> verifyAccount(@Valid @RequestBody VerifyAccountRequest req) {
        commandService.verifyAccount(new VerifyAccountCommand(req.getEmail(), req.getOtp()));
        return ResponseEntity.ok(ApiResponse.ok("Account verified successfully.", null));
    }

    @PostMapping("/resend-verification-otp")
    @Operation(summary = "Resend account verification OTP")
    public ResponseEntity<ApiResponse<Void>> resendVerificationOtp(
            @Valid @RequestBody ResendVerificationRequest req) {
        commandService.resendVerificationOtp(new ResendVerificationCommand(req.getEmail()));
        return ResponseEntity.ok(
                ApiResponse.ok("If the account exists and is eligible, a verification code has been sent.", null));
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

    /**
     * Single refresh endpoint with unambiguous credential source:
     * body refreshToken (native) XOR HttpOnly cookie (web).
     * Conflicting unequal values are rejected.
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token (cookie for web, body for native)")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Optional<String> cookieToken = refreshCookieSupport.readRefreshCookie(request);
        Optional<String> bodyToken = Optional.ofNullable(body)
                .map(RefreshTokenRequest::getRefreshToken);

        AuthTransportResolver.ResolvedRefresh resolved =
                AuthTransportResolver.resolveRefreshCredential(cookieToken, bodyToken);

        AuthResponse res = presentationMapper.toAuthResponse(commandService.refresh(
                RefreshTokenCommand.builder()
                        .refreshToken(resolved.refreshToken())
                        .transport(resolved.transport())
                        .clientPlatform(ClientPlatform.fromTransport(resolved.transport()))
                        .ipAddress(request.getRemoteAddr())
                        .userAgent(request.getHeader("User-Agent"))
                        .build()
        ));

        applyTransportResponse(resolved.transport(), request, response, res);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout current session", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> logout(
            @AuthenticationPrincipal UserDetails principal,
            @RequestBody(required = false) RefreshTokenRequest body,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        UUID userId = commandService.resolveUserId(principal);

        Optional<String> accessJti = commandService.parseOptionalAccessJti(
                request.getHeader("Authorization"),
                userId
        );

        Optional<String> cookieToken = refreshCookieSupport.readRefreshCookie(request);
        Optional<String> bodyToken = Optional.ofNullable(body)
                .map(RefreshTokenRequest::getRefreshToken);
        Optional<String> refreshToken = bodyToken.filter(t -> t != null && !t.isBlank())
                .or(() -> cookieToken);

        commandService.logout(userId, refreshToken, accessJti);
        refreshCookieSupport.clearRefreshCookie(request, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout all sessions", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> logoutAll(
            @AuthenticationPrincipal UserDetails principal,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        UUID userId = commandService.resolveUserId(principal);
        commandService.logoutAll(userId);
        refreshCookieSupport.clearRefreshCookie(request, response);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    @Operation(
            summary = "Change password for the authenticated user",
            description = "Requires the current password. On success, all refresh tokens and access tokens "
                    + "are invalidated (tokenVersion bump). The client must log in again.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ChangePasswordRequest req,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        UUID userId = commandService.resolveUserId(principal);
        ChangePasswordCommand cmd = presentationMapper.toChangePasswordCommand(req, request.getRemoteAddr());
        commandService.changePassword(userId, cmd);
        refreshCookieSupport.clearRefreshCookie(request, response);
        return ResponseEntity.ok(ApiResponse.ok("Password changed successfully", null));
    }

    private void applyTransportResponse(
            AuthTransport transport,
            HttpServletRequest request,
            HttpServletResponse response,
            AuthResponse res
    ) {
        if (transport == AuthTransport.BODY) {
            // Native: refresh stays in JSON; clear any prior web cookie.
            refreshCookieSupport.clearRefreshCookie(request, response);
            return;
        }
        refreshCookieSupport.setRefreshCookie(request, response, res.getRefreshToken());
        res.clearRefreshToken();
    }
}
