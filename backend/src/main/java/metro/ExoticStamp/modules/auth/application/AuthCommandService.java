package metro.ExoticStamp.modules.auth.application;

import metro.ExoticStamp.infra.mail.MailService;
import metro.ExoticStamp.modules.auth.application.command.ChangePasswordCommand;
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
import metro.ExoticStamp.modules.auth.domain.PasswordPolicy;
import metro.ExoticStamp.modules.auth.domain.exception.AccountNotVerifiedException;
import metro.ExoticStamp.modules.auth.domain.exception.CurrentPasswordIncorrectException;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidCredentialsException;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidTokenException;
import metro.ExoticStamp.modules.auth.domain.exception.NewPasswordSameAsCurrentException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpMaxAttemptsExceededException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpExpiredException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpInvalidException;
import metro.ExoticStamp.modules.auth.domain.exception.PasswordConfirmationMismatchException;
import metro.ExoticStamp.modules.auth.domain.exception.RefreshTokenExpiredException;
import metro.ExoticStamp.modules.auth.domain.exception.RefreshTokenRevokedException;
import metro.ExoticStamp.modules.auth.domain.exception.RefreshTokenReusedException;
import metro.ExoticStamp.modules.auth.domain.exception.RefreshUnavailableException;
import metro.ExoticStamp.modules.auth.domain.exception.ResendCooldownException;
import metro.ExoticStamp.modules.auth.domain.exception.TokenExpiredException;
import metro.ExoticStamp.modules.auth.domain.exception.UserNotActiveException;
import metro.ExoticStamp.modules.auth.domain.model.AccessToken;
import metro.ExoticStamp.modules.auth.domain.model.ClientPlatform;
import metro.ExoticStamp.modules.auth.domain.model.OtpType;
import metro.ExoticStamp.modules.auth.application.port.RefreshTokenStorePort.GraceCredentials;
import metro.ExoticStamp.modules.auth.domain.exception.SessionRevokedException;
import metro.ExoticStamp.modules.auth.domain.repository.AccessTokenRepository;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import metro.ExoticStamp.modules.user.application.port.UserAccountPort;
import metro.ExoticStamp.modules.user.domain.event.EmailVerifiedEvent;
import metro.ExoticStamp.modules.user.domain.event.UserCreatedEvent;
import metro.ExoticStamp.modules.user.domain.exception.UserNotFoundException;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.model.UserStatus;
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

import java.time.Duration;
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
    private static final String AUDIT_TABLE_USERS = "users";
    private static final String AUDIT_ACTION_LOGIN = "LOGIN";
    private static final String AUDIT_ACTION_PASSWORD_CHANGED = "PASSWORD_CHANGED";
    private static final String AUDIT_ACTION_REFRESH = "REFRESH";
    private static final String AUDIT_ACTION_LOGOUT = "LOGOUT";
    private static final String AUDIT_ACTION_LOGOUT_ALL = "LOGOUT_ALL";
    private static final String AUDIT_ACTION_REFRESH_REUSE = "REFRESH_REUSE";

    private static final String OTP_CHARS = "0123456789";
    private static final SecureRandom RNG = new SecureRandom();

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserAccountPort userAccountPort;
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
        User user = userAccountPort.findByEmail(cmd.getIdentifier())
                .or(() -> userAccountPort.findByUsername(cmd.getIdentifier()))
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
        UUID sessionId = UUID.randomUUID();
        ClientPlatform platform = cmd.getClientPlatform() != null
                ? cmd.getClientPlatform()
                : ClientPlatform.UNKNOWN;

        AccessToken record = AccessToken.builder()
                .id(sessionId)
                .userId(user.getId())
                .tokenHash(tokenHash)
                .tokenType(TOKEN_TYPE_REFRESH)
                .tokenPrefix(TOKEN_PREFIX_BEARER)
                .tokenFamilyId(sessionId)
                .expiresAt(expiresAt)
                .ipAddress(nullToEmpty(cmd.getIpAddress()))
                .userAgent(nullToEmpty(cmd.getUserAgent()))
                .deviceFingerprint(deviceFingerprint)
                .clientPlatform(platform.name())
                .userAgentHash(hashNullable(cmd.getUserAgent()))
                .ipHash(hashNullable(cmd.getIpAddress()))
                .createdAt(now)
                .version(0L)
                .build();

        accessTokenRepository.save(record);
        runAfterCommit(() -> {
            refreshTokenStore.save(user.getId(), deviceFingerprint, tokenHash);
            accessTokenRevocation.setDeviceAccessJti(user.getId(), deviceFingerprint, issued.jti());
        });
        auditLogService.log(
                user.getId(),
                AUDIT_TABLE_ACCESS_TOKENS,
                AUDIT_ACTION_LOGIN,
                null,
                Map.of(
                        "sessionId", sessionId.toString(),
                        "familyId", sessionId.toString(),
                        "platform", platform.name()
                ),
                cmd.getIpAddress()
        );

        return toAuthView(accessToken, refreshToken, user, roles);
    }

    @Transactional
    public void register(RegisterCommand cmd) {
        if (userAccountPort.existsByEmail(cmd.getEmail())) {
            throw new UserFieldAlreadyTakenException("email", cmd.getEmail());
        }
        if (userAccountPort.existsByUsername(cmd.getUsername())) {
            throw new UserFieldAlreadyTakenException("username", cmd.getUsername());
        }
        if (userAccountPort.existsByPhoneNumber(cmd.getPhoneNumber())) {
            throw new UserFieldAlreadyTakenException("phone", cmd.getPhoneNumber());
        }

        PasswordPolicy.validatePlaintext(cmd.getPassword());

        User user = User.builder()
                .firstname(cmd.getFirstname())
                .lastname(cmd.getLastname())
                .username(cmd.getUsername())
                .email(cmd.getEmail())
                .phoneNumber(cmd.getPhoneNumber())
                .password(passwordEncoder.encode(cmd.getPassword()))
                .status(UserStatus.PENDING_VERIFIED)
                .build();

        User saved = userAccountPort.save(user);

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
        if (otpStore.isMaxAttemptsExceeded(cmd.getEmail(), OtpType.EMAIL_VERIFY)) {
            throw new OtpMaxAttemptsExceededException(
                    authSecurityProperties.getOtp().forType(OtpType.EMAIL_VERIFY).getMaxAttempts()
            );
        }

        String otp = otpStore.find(cmd.getEmail(), OtpType.EMAIL_VERIFY)
                .orElseThrow(OtpExpiredException::new);

        if (!otp.equals(cmd.getOtp())) {
            otpStore.incrementAttempts(cmd.getEmail(), OtpType.EMAIL_VERIFY);
            throw new OtpInvalidException();
        }

        User user = userAccountPort.findByEmail(cmd.getEmail())
                .orElseThrow(() -> new UserNotFoundException("email", cmd.getEmail()));

        if (user.getStatus() == UserStatus.ACTIVE) {
            otpStore.delete(cmd.getEmail(), OtpType.EMAIL_VERIFY);
            return;
        }

        user.setStatus(UserStatus.ACTIVE);
        user.setVerifiedAt(LocalDateTime.now());
        userAccountPort.save(user);

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

        userAccountPort.findByEmail(cmd.getEmail()).ifPresent(user -> {
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

        Optional<User> userOpt = userAccountPort.findByEmail(cmd.getEmail());
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

        userAccountPort.findByEmail(cmd.getEmail()).ifPresent(user -> {
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
        if (otpStore.isMaxAttemptsExceeded(cmd.getEmail(), OtpType.FORGOT_PASSWORD)) {
            throw new OtpMaxAttemptsExceededException(
                    authSecurityProperties.getOtp().forType(OtpType.FORGOT_PASSWORD).getMaxAttempts()
            );
        }

        String otp = otpStore.find(cmd.getEmail(), OtpType.FORGOT_PASSWORD)
                .orElseThrow(OtpExpiredException::new);

        if (!otp.equals(cmd.getOtp())) {
            otpStore.incrementAttempts(cmd.getEmail(), OtpType.FORGOT_PASSWORD);
            throw new OtpInvalidException();
        }

        User user = userAccountPort.findByEmail(cmd.getEmail())
                .orElseThrow(() -> new UserNotFoundException("email", cmd.getEmail()));

        PasswordPolicy.validatePlaintext(cmd.getNewPassword());
        user.setPassword(passwordEncoder.encode(cmd.getNewPassword()));
        user.setPasswordUpdateAt(LocalDateTime.now());
        userAccountPort.save(user);

        accessTokenRepository.revokeAllByUserId(user.getId(), AccessToken.REASON_PASSWORD_RESET);
        bumpTokenVersionAndSyncRedis(user.getId());
        runAfterCommit(() -> refreshTokenStore.revokeAllForUser(user.getId()));
        otpStore.delete(cmd.getEmail(), OtpType.FORGOT_PASSWORD);
    }

    /**
     * Authenticated user changes their own password.
     * Derives identity from {@code authenticatedUserId} only — never from the request body.
     * On success, invalidates all sessions via the same path as logout-all / password-reset.
     */
    @Transactional
    public void changePassword(UUID authenticatedUserId, ChangePasswordCommand cmd) {
        if (cmd.getNewPassword() == null || cmd.getConfirmNewPassword() == null
                || !cmd.getNewPassword().equals(cmd.getConfirmNewPassword())) {
            throw new PasswordConfirmationMismatchException();
        }

        PasswordPolicy.validatePlaintext(cmd.getNewPassword());

        User user = userAccountPort.findById(authenticatedUserId)
                .orElseThrow(() -> new UserNotFoundException(authenticatedUserId));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException();
        }

        if (!passwordEncoder.matches(cmd.getCurrentPassword(), user.getPassword())) {
            log.warn("[Auth] change-password failed: current password incorrect userId={}", authenticatedUserId);
            throw new CurrentPasswordIncorrectException();
        }

        // After current-password verification: reject reuse of the same password.
        if (cmd.getNewPassword().equals(cmd.getCurrentPassword())
                || passwordEncoder.matches(cmd.getNewPassword(), user.getPassword())) {
            throw new NewPasswordSameAsCurrentException();
        }

        user.setPassword(passwordEncoder.encode(cmd.getNewPassword()));
        user.setPasswordUpdateAt(LocalDateTime.now());
        userAccountPort.save(user);

        // Same invalidation path as resetPassword / logoutAll — do not invent a second mechanism.
        accessTokenRepository.revokeAllByUserId(user.getId(), AccessToken.REASON_PASSWORD_CHANGED);
        bumpTokenVersionAndSyncRedis(user.getId());
        runAfterCommit(() -> refreshTokenStore.revokeAllForUser(user.getId()));

        auditLogService.log(
                user.getId(),
                AUDIT_TABLE_USERS,
                AUDIT_ACTION_PASSWORD_CHANGED,
                null,
                Map.of("sessionsRevoked", true),
                cmd.getIpAddress()
        );
    }

    @Transactional
    public AuthView refresh(RefreshTokenCommand cmd) {
        String token = cmd.getRefreshToken();
        if (token == null || token.isBlank()) {
            throw new InvalidTokenException("Refresh token missing");
        }
        String tokenHash = accessTokenPort.hashToken(token);

        Optional<GraceCredentials> graceHit = refreshTokenStore.findGraceCredentials(tokenHash);
        if (graceHit.isPresent()) {
            GraceCredentials grace = graceHit.get();
            UUID userId = accessTokenPort.parseRefreshUserId(grace.refreshToken());
            User user = userAccountPort.findById(userId)
                    .orElseThrow(() -> new InvalidTokenException("User not found"));
            if (user.getStatus() != UserStatus.ACTIVE) {
                throw new UserNotActiveException();
            }
            List<String> roles = roleQueryService.getRoleNamesByUserId(userId);
            return toAuthView(grace.accessToken(), grace.refreshToken(), user, roles);
        }

        if (!accessTokenPort.isTokenValid(token)) {
            // Distinguish expiry when possible
            try {
                accessTokenPort.parseRefreshUserId(token);
            } catch (TokenExpiredException e) {
                throw new RefreshTokenExpiredException();
            } catch (RuntimeException ignored) {
                // fall through
            }
            throw new InvalidTokenException("Invalid token");
        }

        UUID userId = accessTokenPort.parseRefreshUserId(token);
        AccessToken record = accessTokenRepository.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (record.isRevoked()) {
            return handleRevokedRefreshPresentation(tokenHash, record);
        }
        if (record.isExpired()) {
            throw new RefreshTokenExpiredException();
        }

        User user = userAccountPort.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("User not found"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException();
        }
        long tokenVersionAtStart = user.getTokenVersion();

        List<String> roles = roleQueryService.getRoleNamesByUserId(userId);

        accessTokenRevocation.getDeviceAccessJti(userId, record.getDeviceFingerprint())
                .ifPresent(jti -> accessTokenRevocation.addToDenylist(
                        jti,
                        tokenTtlPort.getAccessTokenTtl()
                ));

        // Re-check after lock work / before issuing — refresh vs logout-all / disable races.
        User latest = userAccountPort.findById(userId)
                .orElseThrow(() -> new InvalidTokenException("User not found"));
        if (latest.getStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException();
        }
        if (latest.getTokenVersion() != tokenVersionAtStart) {
            throw new SessionRevokedException();
        }

        IssuedAccessTokenView issued = accessTokenPort.issueAccessToken(latest, roles);
        String newAccessToken = issued.token();
        String newRefreshToken = accessTokenPort.generateRefreshToken(userId);
        String newTokenHash = accessTokenPort.hashToken(newRefreshToken);

        LocalDateTime now = LocalDateTime.now();
        UUID newSessionId = UUID.randomUUID();
        UUID familyId = record.getTokenFamilyId() != null ? record.getTokenFamilyId() : record.getId();

        record.setRevokedAt(now);
        record.setRevokedReason(AccessToken.REASON_ROTATED);
        record.setUsedAt(now);
        record.setReplacedByTokenId(newSessionId);
        accessTokenRepository.save(record);

        String platform = cmd.getClientPlatform() != null
                ? cmd.getClientPlatform().name()
                : (record.getClientPlatform() != null ? record.getClientPlatform() : ClientPlatform.UNKNOWN.name());

        AccessToken newRecord = AccessToken.builder()
                .id(newSessionId)
                .userId(userId)
                .tokenHash(newTokenHash)
                .tokenType(TOKEN_TYPE_REFRESH)
                .tokenPrefix(TOKEN_PREFIX_BEARER)
                .tokenFamilyId(familyId)
                .parentTokenId(record.getId())
                .expiresAt(now.plus(tokenTtlPort.getRefreshTokenTtl()))
                .ipAddress(nullToEmpty(cmd.getIpAddress() != null ? cmd.getIpAddress() : record.getIpAddress()))
                .userAgent(nullToEmpty(cmd.getUserAgent() != null ? cmd.getUserAgent() : record.getUserAgent()))
                .deviceFingerprint(record.getDeviceFingerprint())
                .clientPlatform(platform)
                .userAgentHash(hashNullable(cmd.getUserAgent() != null ? cmd.getUserAgent() : record.getUserAgent()))
                .ipHash(hashNullable(cmd.getIpAddress() != null ? cmd.getIpAddress() : record.getIpAddress()))
                .createdAt(now)
                .version(0L)
                .build();

        accessTokenRepository.save(newRecord);

        Duration grace = authSecurityProperties.getRefreshReuseGrace();
        refreshTokenStore.putGraceCredentials(tokenHash, newAccessToken, newRefreshToken, grace);

        String deviceFp = record.getDeviceFingerprint();
        runAfterCommit(() -> {
            refreshTokenStore.revoke(userId, deviceFp, tokenHash);
            refreshTokenStore.save(userId, deviceFp, newTokenHash);
            accessTokenRevocation.setDeviceAccessJti(userId, deviceFp, issued.jti());
        });

        auditLogService.log(
                userId,
                AUDIT_TABLE_ACCESS_TOKENS,
                AUDIT_ACTION_REFRESH,
                null,
                Map.of(
                        "sessionId", newSessionId.toString(),
                        "familyId", familyId.toString(),
                        "parentSessionId", record.getId().toString()
                ),
                cmd.getIpAddress()
        );

        return toAuthView(newAccessToken, newRefreshToken, latest, roles);
    }

    private AuthView handleRevokedRefreshPresentation(String tokenHash, AccessToken record) {
        if (AccessToken.REASON_ROTATED.equals(record.getRevokedReason())) {
            LocalDateTime revokedAt = record.getRevokedAt();
            Duration grace = authSecurityProperties.getRefreshReuseGrace();
            boolean withinGrace = revokedAt != null
                    && revokedAt.isAfter(LocalDateTime.now().minus(grace));

            if (withinGrace) {
                Optional<GraceCredentials> graceCreds = refreshTokenStore.findGraceCredentials(tokenHash);
                if (graceCreds.isPresent()) {
                    GraceCredentials cached = graceCreds.get();
                    UUID userId = record.getUserId();
                    User user = userAccountPort.findById(userId)
                            .orElseThrow(() -> new InvalidTokenException("User not found"));
                    if (user.getStatus() != UserStatus.ACTIVE) {
                        throw new UserNotActiveException();
                    }
                    List<String> roles = roleQueryService.getRoleNamesByUserId(userId);
                    return toAuthView(cached.accessToken(), cached.refreshToken(), user, roles);
                }
                // DB rotated within grace but Redis grace payload missing → infrastructure uncertainty.
                // Do not classify as reuse/compromise; require safe retry/re-login.
                throw new RefreshUnavailableException();
            }
            handleConfirmedReuse(record);
        }
        throw new RefreshTokenRevokedException();
    }

    private void handleConfirmedReuse(AccessToken record) {
        // Policy B: confirmed reuse outside grace → revoke ALL user sessions + bump tokenVersion.
        UUID familyId = record.getTokenFamilyId() != null ? record.getTokenFamilyId() : record.getId();
        accessTokenRepository.revokeAllByUserId(record.getUserId(), AccessToken.REASON_REUSE_ATTACK);
        runAfterCommit(() -> refreshTokenStore.revokeAllForUser(record.getUserId()));
        bumpTokenVersionAndSyncRedis(record.getUserId());
        auditLogService.log(
                record.getUserId(),
                AUDIT_TABLE_ACCESS_TOKENS,
                AUDIT_ACTION_REFRESH_REUSE,
                null,
                Map.of(
                        "scope", "ALL_SESSIONS",
                        "familyId", familyId.toString(),
                        "tokenVersionBumped", true
                ),
                null
        );
        log.warn("[Auth] refresh reuse outside grace — global revocation userId={} familyId={}",
                record.getUserId(), familyId);
        throw new RefreshTokenReusedException();
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
            auditLogService.log(
                    userId,
                    AUDIT_TABLE_ACCESS_TOKENS,
                    AUDIT_ACTION_LOGOUT,
                    null,
                    Map.of("refreshPresented", false),
                    null
            );
            return;
        }

        String refreshToken = refreshTokenOpt.get();
        String hash = accessTokenPort.hashToken(refreshToken);
        accessTokenRepository.revokeByTokenHash(hash, AccessToken.REASON_LOGOUT);

        Optional<AccessToken> recordOpt = accessTokenRepository.findByTokenHash(hash);
        if (recordOpt.isPresent()) {
            AccessToken record = recordOpt.get();
            String deviceFp = record.getDeviceFingerprint();
            runAfterCommit(() -> {
                refreshTokenStore.revoke(userId, deviceFp, hash);
                accessTokenRevocation.deleteDeviceAccessJti(userId, deviceFp);
            });
        }

        auditLogService.log(
                userId,
                AUDIT_TABLE_ACCESS_TOKENS,
                AUDIT_ACTION_LOGOUT,
                null,
                Map.of("refreshPresented", true),
                null
        );
    }

    @Transactional
    public void logoutAll(UUID userId) {
        accessTokenRepository.revokeAllByUserId(userId, AccessToken.REASON_LOGOUT_ALL);
        bumpTokenVersionAndSyncRedis(userId);
        runAfterCommit(() -> refreshTokenStore.revokeAllForUser(userId));
        auditLogService.log(
                userId,
                AUDIT_TABLE_ACCESS_TOKENS,
                AUDIT_ACTION_LOGOUT_ALL,
                null,
                Map.of("sessionsRevoked", true),
                null
        );
    }

    private void bumpTokenVersionAndSyncRedis(UUID userId) {
        int updated = userAccountPort.incrementTokenVersionById(userId);
        if (updated == 0) {
            return;
        }
        userAccountPort.findTokenVersionById(userId).ifPresent(v -> {
            try {
                accessTokenRevocation.setCachedTokenVersion(userId, v);
            } catch (Exception e) {
                log.warn("[Auth] tokenVersion Redis cache set failed userId={}: {}", userId, e.getMessage());
            }
        });
    }

    private void runAfterCommit(Runnable action) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            action.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                action.run();
            }
        });
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
        return accessTokenPort.hashToken(nullToEmpty(cmd.getUserAgent()) + nullToEmpty(cmd.getIpAddress()));
    }

    private String hashNullable(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return accessTokenPort.hashToken(value);
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private static AuthView toAuthView(String accessToken, String refreshToken, User user, List<String> roles) {
        return new AuthView(
                accessToken,
                refreshToken,
                new AuthUserView(user.getId(), user.getEmail(), user.getUsername(), roles)
        );
    }
}
