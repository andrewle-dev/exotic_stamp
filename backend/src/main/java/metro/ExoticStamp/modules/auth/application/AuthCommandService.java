package metro.ExoticStamp.modules.auth.application;

import metro.ExoticStamp.infra.mail.MailService;
import metro.ExoticStamp.modules.auth.application.command.ForgotPasswordCommand;
import metro.ExoticStamp.modules.auth.application.command.LoginCommand;
import metro.ExoticStamp.modules.auth.application.command.RefreshTokenCommand;
import metro.ExoticStamp.modules.auth.application.command.RegisterCommand;
import metro.ExoticStamp.modules.auth.application.command.ResendOtpCommand;
import metro.ExoticStamp.modules.auth.application.command.ResendVerificationCommand;
import metro.ExoticStamp.modules.auth.application.command.ResetPasswordCommand;
import metro.ExoticStamp.modules.auth.application.command.VerifyAccountCommand;
import metro.ExoticStamp.modules.auth.application.port.AccessTokenPort;
import metro.ExoticStamp.modules.auth.application.port.AccessTokenRevocationPort;
import metro.ExoticStamp.modules.auth.application.port.OtpStorePort;
import metro.ExoticStamp.modules.auth.application.port.RefreshTokenStorePort;
import metro.ExoticStamp.modules.auth.application.port.TokenTtlPort;
import metro.ExoticStamp.modules.auth.application.view.AuthUserView;
import metro.ExoticStamp.modules.auth.application.view.AuthView;
import metro.ExoticStamp.modules.auth.application.view.IssuedAccessTokenView;
import metro.ExoticStamp.modules.auth.config.AuthSecurityProperties;
import metro.ExoticStamp.modules.auth.domain.exception.AccountNotVerifiedException;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidCredentialsException;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidTokenException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpMaxAttemptsExceededException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpExpiredException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpInvalidException;
import metro.ExoticStamp.modules.auth.domain.exception.ResendCooldownException;
import metro.ExoticStamp.modules.auth.domain.exception.SecurityBreachException;
import metro.ExoticStamp.modules.auth.domain.exception.TokenExpiredException;
import metro.ExoticStamp.modules.auth.domain.exception.UserNotActiveException;
import metro.ExoticStamp.modules.auth.domain.model.AccessToken;
import metro.ExoticStamp.modules.auth.domain.model.OtpType;
import metro.ExoticStamp.modules.auth.domain.repository.AccessTokenRepository;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import metro.ExoticStamp.modules.user.domain.event.EmailVerifiedEvent;
import metro.ExoticStamp.modules.user.domain.event.UserCreatedEvent;
import metro.ExoticStamp.modules.user.domain.exception.UserNotFoundException;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.model.UserStatus;
import metro.ExoticStamp.modules.user.domain.repository.UserRepository;
import metro.ExoticStamp.modules.user.domain.exception.UserFieldAlreadyTakenException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthCommandService {

    private static final String TOKEN_TYPE_REFRESH = "REFRESH";
    private static final String TOKEN_PREFIX_BEARER = "Bearer";

    private static final String AUDIT_TABLE_ACCESS_TOKENS = "access_tokens";
    private static final String AUDIT_ACTION_LOGIN = "LOGIN";

    private static final String OTP_CHARS = "0123456789";
    private static final SecureRandom RNG = new SecureRandom();

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserRepository userRepository;
    private final AccessTokenRepository accessTokenRepository;
    private final RoleQueryService roleQueryService;
    private final AccessTokenPort accessTokenPort;
    private final RefreshTokenStorePort refreshTokenStore;
    private final AccessTokenRevocationPort accessTokenRevocation;
    private final OtpStorePort otpStore;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;
    private final ApplicationEventPublisher eventPublisher;
    private final TokenTtlPort tokenTtlPort;
    private final MailService mailService;
    private final AuthSecurityProperties authSecurityProperties;

    @Transactional
    public AuthView login(LoginCommand cmd) {
        User user = userRepository.findByEmail(cmd.getIdentifier())
                .or(() -> userRepository.findByUsername(cmd.getIdentifier()))
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getStatus() == UserStatus.PENDING_VERIFIED) {
            throw new AccountNotVerifiedException();
        }

        if (!passwordEncoder.matches(cmd.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException();
        }

        List<String> roles = roleQueryService.getRoleNamesByUserId(user.getId());

        IssuedAccessTokenView issued = accessTokenPort.issueAccessToken(user, roles);
        String accessToken = issued.token();
        String refreshToken = accessTokenPort.generateRefreshToken(user.getId());
        String tokenHash = accessTokenPort.hashToken(refreshToken);

        LocalDateTime now = LocalDateTime.now();
        String deviceFingerprint = normalizeDeviceFingerprint(cmd);
        LocalDateTime expiresAt = now.plus(tokenTtlPort.getRefreshTokenTtl());

        AccessToken record = AccessToken.builder()
                .id(UUID.randomUUID())
                .userId(user.getId())
                .tokenHash(tokenHash)
                .tokenType(TOKEN_TYPE_REFRESH)
                .tokenPrefix(TOKEN_PREFIX_BEARER)
                .expiresAt(expiresAt)
                .ipAddress(cmd.getIpAddress())
                .userAgent(cmd.getUserAgent())
                .deviceFingerprint(deviceFingerprint)
                .createdAt(now)
                .build();

        accessTokenRepository.save(record);
        refreshTokenStore.save(user.getId(), deviceFingerprint, tokenHash);
        accessTokenRevocation.setDeviceAccessJti(user.getId(), deviceFingerprint, issued.jti());
        auditLogService.log(
                user.getId(),
                AUDIT_TABLE_ACCESS_TOKENS,
                AUDIT_ACTION_LOGIN,
                null,
                record,
                cmd.getIpAddress()
        );

        return toAuthView(accessToken, refreshToken, user, roles);
    }

    @Transactional
    public void register(RegisterCommand cmd) {
        if (userRepository.existsByEmail(cmd.getEmail())) {
            throw new UserFieldAlreadyTakenException("email", cmd.getEmail());
        }
        if (userRepository.existsByUsername(cmd.getUsername())) {
            throw new UserFieldAlreadyTakenException("username", cmd.getUsername());
        }
        if (userRepository.existsByPhoneNumber(cmd.getPhoneNumber())) {
            throw new UserFieldAlreadyTakenException("phone", cmd.getPhoneNumber());
        }

        User user = User.builder()
                .firstname(cmd.getFirstname())
                .lastname(cmd.getLastname())
                .username(cmd.getUsername())
                .email(cmd.getEmail())
                .phoneNumber(cmd.getPhoneNumber())
                .password(passwordEncoder.encode(cmd.getPassword()))
                .status(UserStatus.PENDING_VERIFIED)
                .build();

        User saved = userRepository.save(user);

        String otp = generateOtp();
        otpStore.delete(saved.getEmail(), OtpType.EMAIL_VERIFY);
        otpStore.save(saved.getEmail(), OtpType.EMAIL_VERIFY, otp);
        otpStore.saveCooldown(saved.getEmail(), OtpType.EMAIL_VERIFY);
        otpStore.incrementAttempts(saved.getEmail(), OtpType.EMAIL_VERIFY);

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                eventPublisher.publishEvent(new UserCreatedEvent(saved, otp));
            }
        });
    }

    @Transactional
    public void verifyAccount(VerifyAccountCommand cmd) {
        String otp = otpStore.find(cmd.getEmail(), OtpType.EMAIL_VERIFY)
                .orElseThrow(OtpExpiredException::new);

        if (!otp.equals(cmd.getOtp())) {
            throw new OtpInvalidException();
        }

        User user = userRepository.findByEmail(cmd.getEmail())
                .orElseThrow(() -> new UserNotFoundException("email", cmd.getEmail()));

        if (user.getStatus() == UserStatus.ACTIVE) {
            otpStore.delete(cmd.getEmail(), OtpType.EMAIL_VERIFY);
            return;
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setVerifiedAt(LocalDateTime.now());
        userRepository.save(user);

        otpStore.delete(cmd.getEmail(), OtpType.EMAIL_VERIFY);

        UUID verifiedUserId = user.getId();
        RbacTransactionCallbacks.afterCommit(() -> {
            try {
                eventPublisher.publishEvent(new EmailVerifiedEvent(verifiedUserId));
            } catch (Exception e) {
                log.error("[Auth] EmailVerifiedEvent publish failed userId={}: {}", verifiedUserId, e.getMessage(), e);
            }
        });
    }

    @Transactional
    public void resendVerificationOtp(ResendVerificationCommand cmd) {
        if (otpStore.isOnCooldown(cmd.getEmail(), OtpType.EMAIL_VERIFY)) {
            long secondsLeft = otpStore.getCooldownTtlSeconds(cmd.getEmail(), OtpType.EMAIL_VERIFY);
            throw new ResendCooldownException(secondsLeft);
        }

        if (otpStore.isMaxAttemptsExceeded(cmd.getEmail(), OtpType.EMAIL_VERIFY)) {
            throw new OtpMaxAttemptsExceededException(
                    authSecurityProperties.getOtp().forType(OtpType.EMAIL_VERIFY).getMaxAttempts()
            );
        }

        userRepository.findByEmail(cmd.getEmail()).ifPresent(user -> {
            if (user.getStatus() != UserStatus.PENDING_VERIFIED) {
                return;
            }

            otpStore.delete(cmd.getEmail(), OtpType.EMAIL_VERIFY);

            String otp = generateOtp();
            otpStore.save(cmd.getEmail(), OtpType.EMAIL_VERIFY, otp);
            otpStore.saveCooldown(cmd.getEmail(), OtpType.EMAIL_VERIFY);
            otpStore.incrementAttempts(cmd.getEmail(), OtpType.EMAIL_VERIFY);

            mailService.sendVerifyAccountOtp(cmd.getEmail(), user.getUsername(), otp);

            auditLogService.log(
                    user.getId(),
                    "otp",
                    "RESEND_VERIFY_ACCOUNT_OTP",
                    null,
                    Map.of(
                            "email", cmd.getEmail(),
                            "attempts", otpStore.getAttemptsCount(cmd.getEmail(), OtpType.EMAIL_VERIFY)
                    ),
                    "SYSTEM"
            );
        });
    }

    @Transactional
    public void forgotPassword(ForgotPasswordCommand cmd) {
        if (otpStore.isOnCooldown(cmd.getEmail(), OtpType.FORGOT_PASSWORD)) {
            return;
        }
        if (otpStore.isMaxAttemptsExceeded(cmd.getEmail(), OtpType.FORGOT_PASSWORD)) {
            return;
        }

        Optional<User> userOpt = userRepository.findByEmail(cmd.getEmail());
        if (userOpt.isEmpty()) {
            return;
        }

        User user = userOpt.get();

        otpStore.delete(user.getEmail(), OtpType.FORGOT_PASSWORD);
        String otp = generateOtp();
        otpStore.save(user.getEmail(), OtpType.FORGOT_PASSWORD, otp);
        otpStore.saveCooldown(user.getEmail(), OtpType.FORGOT_PASSWORD);
        otpStore.incrementAttempts(user.getEmail(), OtpType.FORGOT_PASSWORD);
        mailService.sendOtpEmail(cmd.getEmail(), otp);
    }

    @Transactional
    public void resendForgotPasswordOtp(ResendOtpCommand cmd) {
        if (otpStore.isOnCooldown(cmd.getEmail(), OtpType.FORGOT_PASSWORD)) {
            long secondsLeft = otpStore.getCooldownTtlSeconds(cmd.getEmail(), OtpType.FORGOT_PASSWORD);
            throw new ResendCooldownException(secondsLeft);
        }

        if (otpStore.isMaxAttemptsExceeded(cmd.getEmail(), OtpType.FORGOT_PASSWORD)) {
            throw new OtpMaxAttemptsExceededException(
                    authSecurityProperties.getOtp().forType(OtpType.FORGOT_PASSWORD).getMaxAttempts()
            );
        }

        userRepository.findByEmail(cmd.getEmail()).ifPresent(user -> {
            otpStore.delete(cmd.getEmail(), OtpType.FORGOT_PASSWORD);

            String otp = generateOtp();
            otpStore.save(cmd.getEmail(), OtpType.FORGOT_PASSWORD, otp);

            otpStore.saveCooldown(cmd.getEmail(), OtpType.FORGOT_PASSWORD);
            otpStore.incrementAttempts(cmd.getEmail(), OtpType.FORGOT_PASSWORD);

            mailService.sendOtpEmail(cmd.getEmail(), otp);

            auditLogService.log(
                    user.getId(),
                    "otp",
                    "RESEND_FORGOT_PASSWORD_OTP",
                    null,
                    Map.of(
                            "email", cmd.getEmail(),
                            "attempts", otpStore.getAttemptsCount(cmd.getEmail(), OtpType.FORGOT_PASSWORD)
                    ),
                    "SYSTEM"
            );
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordCommand cmd) {
        String otp = otpStore.find(cmd.getEmail(), OtpType.FORGOT_PASSWORD)
                .orElseThrow(OtpExpiredException::new);

        if (!otp.equals(cmd.getOtp())) {
            throw new OtpInvalidException();
        }

        User user = userRepository.findByEmail(cmd.getEmail())
                .orElseThrow(() -> new UserNotFoundException("email", cmd.getEmail()));

        user.setPassword(passwordEncoder.encode(cmd.getNewPassword()));
        user.setPasswordUpdateAt(LocalDateTime.now());
        userRepository.save(user);

        accessTokenRepository.revokeAllByUserId(user.getId(), AccessToken.REASON_PASSWORD_RESET);
        refreshTokenStore.revokeAllForUser(user.getId());
        bumpTokenVersionAndSyncRedis(user.getId());
        otpStore.delete(cmd.getEmail(), OtpType.FORGOT_PASSWORD);
    }

    @Transactional
    public AuthView refresh(RefreshTokenCommand cmd) {
        String token = cmd.getRefreshToken();
        if (token == null || token.isBlank()) {
            throw new InvalidTokenException("Refresh token missing");
        }
        String tokenHash = accessTokenPort.hashToken(token);

        if (refreshTokenStore.isRevoked(tokenHash)) {
            UUID userId = accessTokenPort.extractUserId(token);
            handleReuseAttack(userId);
        }

        if (!accessTokenPort.isTokenValid(token)) {
            throw new InvalidTokenException("Invalid token");
        }

        UUID userId = accessTokenPort.extractUserId(token);
        AccessToken record = accessTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (!record.isValid()) {
            throw new InvalidTokenException("Refresh token is not valid");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("User not found"));

        List<String> roles = roleQueryService.getRoleNamesByUserId(userId);

        accessTokenRevocation.getDeviceAccessJti(userId, record.getDeviceFingerprint())
                .ifPresent(jti -> accessTokenRevocation.addToDenylist(
                        jti,
                        tokenTtlPort.getAccessTokenTtl()
                ));

        IssuedAccessTokenView issued = accessTokenPort.issueAccessToken(user, roles);
        String newAccessToken = issued.token();
        String newRefreshToken = accessTokenPort.generateRefreshToken(userId);
        String newTokenHash = accessTokenPort.hashToken(newRefreshToken);

        accessTokenRepository.revokeByTokenHash(tokenHash, AccessToken.REASON_ROTATED);
        refreshTokenStore.revoke(userId, record.getDeviceFingerprint(), tokenHash);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plus(tokenTtlPort.getRefreshTokenTtl());

        AccessToken newRecord = AccessToken.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .tokenHash(newTokenHash)
                .tokenType(TOKEN_TYPE_REFRESH)
                .tokenPrefix(TOKEN_PREFIX_BEARER)
                .expiresAt(expiresAt)
                .ipAddress(record.getIpAddress())
                .userAgent(record.getUserAgent())
                .deviceFingerprint(record.getDeviceFingerprint())
                .createdAt(now)
                .build();

        accessTokenRepository.save(newRecord);
        refreshTokenStore.save(userId, record.getDeviceFingerprint(), newTokenHash);
        accessTokenRevocation.setDeviceAccessJti(userId, record.getDeviceFingerprint(), issued.jti());

        return toAuthView(newAccessToken, newRefreshToken, user, roles);
    }

    public UUID resolveUserId(UserDetails principal) {
        if (principal == null) {
            throw new IllegalStateException("Missing principal");
        }
        if (principal instanceof User user) {
            return user.getId();
        }
        throw new IllegalStateException("Unsupported principal type: " + principal.getClass().getName());
    }

    public Optional<String> parseOptionalAccessJti(String authorizationHeader, UUID expectedUserId) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            return Optional.empty();
        }
        try {
            var parsed = accessTokenPort.parseAccessToken(
                    authorizationHeader.substring(BEARER_PREFIX.length())
            );
            if (!parsed.userId().equals(expectedUserId)) {
                throw new InvalidTokenException("Access token does not match session user");
            }
            return Optional.of(parsed.jti());
        } catch (InvalidTokenException | TokenExpiredException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public void logout(UUID userId, Optional<String> refreshTokenOpt, Optional<String> accessJti) {
        accessJti.ifPresent(jti -> accessTokenRevocation.addToDenylist(
                jti,
                tokenTtlPort.getAccessTokenTtl()
        ));

        if (refreshTokenOpt.isEmpty()) {
            return;
        }

        String refreshToken = refreshTokenOpt.get();
        String hash = accessTokenPort.hashToken(refreshToken);
        accessTokenRepository.revokeByTokenHash(hash, AccessToken.REASON_LOGOUT);

        Optional<AccessToken> recordOpt = accessTokenRepository.findByTokenHash(hash);
        if (recordOpt.isEmpty()) {
            return;
        }

        AccessToken record = recordOpt.get();
        refreshTokenStore.revoke(userId, record.getDeviceFingerprint(), hash);
        accessTokenRevocation.deleteDeviceAccessJti(userId, record.getDeviceFingerprint());
    }

    @Transactional
    public void logoutAll(UUID userId) {
        accessTokenRepository.revokeAllByUserId(userId, AccessToken.REASON_LOGOUT_ALL);
        refreshTokenStore.revokeAllForUser(userId);
        bumpTokenVersionAndSyncRedis(userId);
    }

    private void handleReuseAttack(UUID userId) {
        accessTokenRepository.revokeAllByUserId(userId, AccessToken.REASON_REUSE_ATTACK);
        refreshTokenStore.revokeAllForUser(userId);
        bumpTokenVersionAndSyncRedis(userId);
        log.error("[Auth] REUSE ATTACK detected userId={}", userId);
        throw new SecurityBreachException(userId.toString());
    }

    private void bumpTokenVersionAndSyncRedis(UUID userId) {
        int updated = userRepository.incrementTokenVersionById(userId);
        if (updated == 0) {
            return;
        }
        userRepository.findTokenVersionById(userId)
                .ifPresent(v -> accessTokenRevocation.setCachedTokenVersion(userId, v));
    }

    private String generateOtp() {
        int otpLength = authSecurityProperties.getOtp().getLength();
        StringBuilder otp = new StringBuilder(otpLength);
        for (int i = 0; i < otpLength; i++) {
            otp.append(OTP_CHARS.charAt(RNG.nextInt(OTP_CHARS.length())));
        }
        return otp.toString();
    }

    private String normalizeDeviceFingerprint(LoginCommand cmd) {
        String fp = cmd.getDeviceFingerprint();
        if (fp != null && !fp.isBlank()) {
            return fp;
        }
        return accessTokenPort.hashToken(cmd.getUserAgent() + cmd.getIpAddress());
    }

    private static AuthView toAuthView(String accessToken, String refreshToken, User user, List<String> roles) {
        return new AuthView(
                accessToken,
                refreshToken,
                new AuthUserView(user.getId(), user.getEmail(), user.getUsername(), roles)
        );
    }
}
