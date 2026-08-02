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
import metro.ExoticStamp.modules.auth.application.view.AuthView;
import metro.ExoticStamp.modules.auth.application.view.IssuedAccessTokenView;
import metro.ExoticStamp.modules.auth.config.AuthSecurityProperties;
import metro.ExoticStamp.modules.auth.domain.exception.AccountNotVerifiedException;
import metro.ExoticStamp.modules.auth.domain.exception.CurrentPasswordIncorrectException;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidCredentialsException;
import metro.ExoticStamp.modules.auth.domain.exception.NewPasswordSameAsCurrentException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpExpiredException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpInvalidException;
import metro.ExoticStamp.modules.auth.domain.exception.PasswordConfirmationMismatchException;
import metro.ExoticStamp.modules.auth.domain.exception.PasswordPolicyViolationException;
import metro.ExoticStamp.modules.auth.domain.exception.ResendCooldownException;
import metro.ExoticStamp.modules.auth.domain.exception.RefreshTokenReusedException;
import metro.ExoticStamp.modules.auth.domain.exception.RefreshTokenRevokedException;
import metro.ExoticStamp.modules.auth.domain.exception.RefreshUnavailableException;
import metro.ExoticStamp.common.exceptions.security.SecurityDependencyUnavailableException;
import metro.ExoticStamp.modules.auth.application.port.RefreshTokenStorePort.GraceCredentials;
import metro.ExoticStamp.modules.auth.domain.exception.SecurityBreachException;
import metro.ExoticStamp.modules.auth.domain.exception.SessionRevokedException;
import metro.ExoticStamp.modules.auth.domain.exception.UserNotActiveException;
import metro.ExoticStamp.modules.auth.domain.model.AccessToken;
import metro.ExoticStamp.modules.auth.domain.model.OtpType;
import metro.ExoticStamp.modules.auth.domain.repository.AccessTokenRepository;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.modules.user.application.port.UserAccountPort;
import metro.ExoticStamp.modules.user.domain.exception.UserFieldAlreadyTakenException;
import metro.ExoticStamp.modules.user.domain.exception.UserNotFoundException;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.model.UserStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import metro.ExoticStamp.modules.auth.application.view.ParsedAccessTokenView;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidTokenException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
class AuthCommandServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock private UserAccountPort userAccountPort;
    @Mock private AccessTokenRepository accessTokenRepository;
    @Mock private RoleQueryService roleQueryService;
    @Mock private AccessTokenPort accessTokenPort;
    @Mock private RefreshTokenStorePort refreshTokenStore;
    @Mock private AccessTokenRevocationPort accessTokenRevocation;
    @Mock private OtpStorePort otpStore;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditLogService auditLogService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private TokenTtlPort tokenTtlPort;
    @Mock private MailService mailService;
    @Mock private AuthSecurityProperties authSecurityProperties;

    @InjectMocks
    private AuthCommandService authCommandService;

    private User activeUser;
    private AuthSecurityProperties.Otp otpProperties;

    @BeforeEach
    void setUp() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.initSynchronization();
        }
        activeUser = User.builder()
                .email("user@test.com")
                .username("user1")
                .phoneNumber("+10000000001")
                .password("encoded")
                .status(UserStatus.ACTIVE)
                .build();
        activeUser.setId(USER_ID);
        otpProperties = new AuthSecurityProperties.Otp();
        lenient().when(authSecurityProperties.getOtp()).thenReturn(otpProperties);
        lenient().when(authSecurityProperties.getRefreshReuseGrace()).thenReturn(Duration.ofSeconds(30));
        lenient().when(refreshTokenStore.findGraceCredentials(any())).thenReturn(Optional.empty());
    }

    @AfterEach
    void clearTransactionSync() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clear();
        }
    }

    @Test
    void register_success_savesUserAndVerificationOtp() {
        RegisterCommand cmd = RegisterCommand.builder()
                .email("new@test.com")
                .username("newuser")
                .password("secret12")
                .firstname("A")
                .lastname("B")
                .phoneNumber("+10000000001")
                .build();
        when(userAccountPort.existsByEmail(cmd.getEmail())).thenReturn(false);
        when(userAccountPort.existsByUsername(cmd.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(cmd.getPassword())).thenReturn("hash");
        when(userAccountPort.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(USER_ID);
            return u;
        });

        authCommandService.register(cmd);

        verify(otpStore).delete("new@test.com", OtpType.EMAIL_VERIFY);
        verify(otpStore).save(eq("new@test.com"), eq(OtpType.EMAIL_VERIFY), anyString());
        verify(otpStore).saveCooldown("new@test.com", OtpType.EMAIL_VERIFY);
        verify(otpStore).incrementAttempts("new@test.com", OtpType.EMAIL_VERIFY);
        verify(userAccountPort).save(argThat(u -> u.getStatus() == UserStatus.PENDING_VERIFIED));
    }

    @Test
    void register_duplicateEmail_throws() {
        when(userAccountPort.existsByEmail("dup@test.com")).thenReturn(true);
        RegisterCommand cmd = RegisterCommand.builder()
                .email("dup@test.com")
                .username("u")
                .password("p")
                .build();
        assertThrows(UserFieldAlreadyTakenException.class, () -> authCommandService.register(cmd));
    }

    @Test
    void register_duplicateUsername_throws() {
        when(userAccountPort.existsByEmail("a@test.com")).thenReturn(false);
        when(userAccountPort.existsByUsername("taken")).thenReturn(true);
        RegisterCommand cmd = RegisterCommand.builder()
                .email("a@test.com")
                .username("taken")
                .password("p")
                .build();
        assertThrows(UserFieldAlreadyTakenException.class, () -> authCommandService.register(cmd));
    }

    @Test
    void register_duplicatePhone_throws() {
        when(userAccountPort.existsByEmail("a@test.com")).thenReturn(false);
        when(userAccountPort.existsByUsername("user")).thenReturn(false);
        when(userAccountPort.existsByPhoneNumber("+10000000009")).thenReturn(true);
        RegisterCommand cmd = RegisterCommand.builder()
                .email("a@test.com")
                .username("user")
                .phoneNumber("+10000000009")
                .password("p")
                .build();
        assertThrows(UserFieldAlreadyTakenException.class, () -> authCommandService.register(cmd));
    }

    @Test
    void login_success_returnsAuthView() {
        LoginCommand cmd = LoginCommand.builder()
                .identifier("user@test.com")
                .password("secret")
                .ipAddress("127.0.0.1")
                .userAgent("test")
                .deviceFingerprint("12345678901234567890")
                .build();
        when(userAccountPort.findByEmail(cmd.getIdentifier())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(cmd.getPassword(), activeUser.getPassword())).thenReturn(true);
        when(roleQueryService.getRoleNamesByUserId(USER_ID)).thenReturn(List.of("USER"));
        when(accessTokenPort.issueAccessToken(eq(activeUser), anyList()))
                .thenReturn(new IssuedAccessTokenView("access", "jti-1"));
        when(accessTokenPort.generateRefreshToken(USER_ID)).thenReturn("refresh");
        when(accessTokenPort.hashToken("refresh")).thenReturn("hash");
        when(tokenTtlPort.getRefreshTokenTtl()).thenReturn(Duration.ofDays(7));

        AuthView view = authCommandService.login(cmd);

        assertEquals("access", view.accessToken());
        assertEquals("refresh", view.refreshToken());
        verify(refreshTokenStore).save(eq(USER_ID), anyString(), eq("hash"));
    }

    @Test
    void login_wrongPassword_throws() {
        LoginCommand cmd = LoginCommand.builder()
                .identifier("user@test.com")
                .password("wrong")
                .build();
        when(userAccountPort.findByEmail(cmd.getIdentifier())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(cmd.getPassword(), activeUser.getPassword())).thenReturn(false);
        assertThrows(InvalidCredentialsException.class, () -> authCommandService.login(cmd));
    }

    @Test
    void login_unverifiedUser_throwsAccountNotVerified() {
        activeUser.setStatus(UserStatus.PENDING_VERIFIED);
        LoginCommand cmd = LoginCommand.builder()
                .identifier("user@test.com")
                .password("secret")
                .build();
        when(userAccountPort.findByEmail(cmd.getIdentifier())).thenReturn(Optional.of(activeUser));
        assertThrows(AccountNotVerifiedException.class, () -> authCommandService.login(cmd));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_unverifiedUser_wrongPassword_stillThrowsAccountNotVerified() {
        activeUser.setStatus(UserStatus.PENDING_VERIFIED);
        LoginCommand cmd = LoginCommand.builder()
                .identifier("user@test.com")
                .password("wrong")
                .build();
        when(userAccountPort.findByEmail(cmd.getIdentifier())).thenReturn(Optional.of(activeUser));
        assertThrows(AccountNotVerifiedException.class, () -> authCommandService.login(cmd));
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_suspendedUser_throwsUserNotActive() {
        activeUser.setStatus(UserStatus.SUSPENDED);
        LoginCommand cmd = LoginCommand.builder()
                .identifier("user@test.com")
                .password("secret")
                .build();
        when(userAccountPort.findByEmail(cmd.getIdentifier())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(cmd.getPassword(), activeUser.getPassword())).thenReturn(true);
        assertThrows(UserNotActiveException.class, () -> authCommandService.login(cmd));
    }

    @Test
    void verifyAccount_success_activatesUser() {
        activeUser.setStatus(UserStatus.PENDING_VERIFIED);
        when(otpStore.find("user@test.com", OtpType.EMAIL_VERIFY)).thenReturn(Optional.of("123456"));
        when(userAccountPort.findByEmail("user@test.com")).thenReturn(Optional.of(activeUser));

        authCommandService.verifyAccount(new VerifyAccountCommand("user@test.com", "123456"));

        assertEquals(UserStatus.ACTIVE, activeUser.getStatus());
        assertNotNull(activeUser.getVerifiedAt());
        verify(otpStore).delete("user@test.com", OtpType.EMAIL_VERIFY);
    }

    @Test
    void verifyAccount_expiredOtp_throws() {
        when(otpStore.find("user@test.com", OtpType.EMAIL_VERIFY)).thenReturn(Optional.empty());
        assertThrows(OtpExpiredException.class,
                () -> authCommandService.verifyAccount(new VerifyAccountCommand("user@test.com", "123456")));
    }

    @Test
    void verifyAccount_wrongOtp_throws() {
        when(otpStore.find("user@test.com", OtpType.EMAIL_VERIFY)).thenReturn(Optional.of("111111"));
        assertThrows(OtpInvalidException.class,
                () -> authCommandService.verifyAccount(new VerifyAccountCommand("user@test.com", "222222")));
        verify(otpStore).incrementAttempts("user@test.com", OtpType.EMAIL_VERIFY);
    }

    @Test
    void verifyAccount_rejectsForgotPasswordOtp() {
        when(otpStore.find("user@test.com", OtpType.EMAIL_VERIFY)).thenReturn(Optional.empty());

        assertThrows(OtpExpiredException.class,
                () -> authCommandService.verifyAccount(new VerifyAccountCommand("user@test.com", "123456")));

        verify(otpStore).find("user@test.com", OtpType.EMAIL_VERIFY);
        verify(otpStore, never()).find("user@test.com", OtpType.FORGOT_PASSWORD);
    }

    @Test
    void refresh_success_rotatesTokens() {
        String oldRefresh = "old-refresh";
        String hash = "old-hash";
        UUID sessionId = UUID.randomUUID();
        AccessToken record = AccessToken.builder()
                .id(sessionId)
                .userId(USER_ID)
                .tokenHash(hash)
                .tokenFamilyId(sessionId)
                .deviceFingerprint("fp")
                .ipAddress("127.0.0.1")
                .userAgent("ua")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .version(0L)
                .build();
        when(accessTokenPort.hashToken(oldRefresh)).thenReturn(hash);
        when(refreshTokenStore.findGraceCredentials(hash)).thenReturn(Optional.empty());
        when(accessTokenPort.isTokenValid(oldRefresh)).thenReturn(true);
        when(accessTokenPort.parseRefreshUserId(oldRefresh)).thenReturn(USER_ID);
        when(accessTokenRepository.findByTokenHashForUpdate(hash)).thenReturn(Optional.of(record));
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(activeUser));
        when(roleQueryService.getRoleNamesByUserId(USER_ID)).thenReturn(List.of("USER"));
        when(accessTokenRevocation.getDeviceAccessJti(USER_ID, "fp")).thenReturn(Optional.of("old-jti"));
        when(accessTokenPort.issueAccessToken(eq(activeUser), anyList()))
                .thenReturn(new IssuedAccessTokenView("new-access", "new-jti"));
        when(accessTokenPort.generateRefreshToken(USER_ID)).thenReturn("new-refresh");
        when(accessTokenPort.hashToken("new-refresh")).thenReturn("new-hash");
        when(tokenTtlPort.getRefreshTokenTtl()).thenReturn(Duration.ofDays(7));
        when(tokenTtlPort.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));
        when(authSecurityProperties.getRefreshReuseGrace()).thenReturn(Duration.ofSeconds(30));

        AuthView view = authCommandService.refresh(RefreshTokenCommand.builder()
                .refreshToken(oldRefresh)
                .build());

        assertEquals("new-access", view.accessToken());
        assertEquals(AccessToken.REASON_ROTATED, record.getRevokedReason());
        assertNotNull(record.getReplacedByTokenId());
        verify(accessTokenRepository, atLeastOnce()).save(any(AccessToken.class));
        verify(refreshTokenStore).putGraceCredentials(eq(hash), eq("new-access"), eq("new-refresh"), any());
    }

    @Test
    void refresh_reuseOutsideGrace_revokesAllSessions() {
        String token = "reused";
        String hash = "h";
        UUID familyId = UUID.randomUUID();
        AccessToken record = AccessToken.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .tokenHash(hash)
                .tokenFamilyId(familyId)
                .deviceFingerprint("fp")
                .ipAddress("127.0.0.1")
                .userAgent("ua")
                .revokedAt(LocalDateTime.now().minusMinutes(5))
                .revokedReason(AccessToken.REASON_ROTATED)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now().minusDays(1))
                .version(0L)
                .build();
        when(accessTokenPort.hashToken(token)).thenReturn(hash);
        when(refreshTokenStore.findGraceCredentials(hash)).thenReturn(Optional.empty());
        when(accessTokenPort.isTokenValid(token)).thenReturn(true);
        when(accessTokenPort.parseRefreshUserId(token)).thenReturn(USER_ID);
        when(accessTokenRepository.findByTokenHashForUpdate(hash)).thenReturn(Optional.of(record));
        when(authSecurityProperties.getRefreshReuseGrace()).thenReturn(Duration.ofSeconds(30));
        when(userAccountPort.incrementTokenVersionById(USER_ID)).thenReturn(1);
        when(userAccountPort.findTokenVersionById(USER_ID)).thenReturn(Optional.of(2L));

        assertThrows(RefreshTokenReusedException.class,
                () -> authCommandService.refresh(RefreshTokenCommand.builder().refreshToken(token).build()));
        verify(accessTokenRepository).revokeAllByUserId(USER_ID, AccessToken.REASON_REUSE_ATTACK);
        verify(auditLogService).log(
                eq(USER_ID),
                anyString(),
                anyString(),
                isNull(),
                argThat(newValue -> {
                    if (!(newValue instanceof java.util.Map<?, ?> map)) {
                        return false;
                    }
                    return "ALL_SESSIONS".equals(map.get("scope"))
                            && Boolean.TRUE.equals(map.get("tokenVersionBumped"));
                }),
                isNull()
        );
    }

    @Test
    void refresh_revokedLogout_throwsRefreshTokenRevoked() {
        String token = "logged-out";
        String hash = "h-logout";
        AccessToken record = AccessToken.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .tokenHash(hash)
                .tokenFamilyId(UUID.randomUUID())
                .deviceFingerprint("fp")
                .revokedAt(LocalDateTime.now().minusMinutes(10))
                .revokedReason(AccessToken.REASON_LOGOUT)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now().minusDays(1))
                .version(0L)
                .build();
        when(accessTokenPort.hashToken(token)).thenReturn(hash);
        when(refreshTokenStore.findGraceCredentials(hash)).thenReturn(Optional.empty());
        when(accessTokenPort.isTokenValid(token)).thenReturn(true);
        when(accessTokenPort.parseRefreshUserId(token)).thenReturn(USER_ID);
        when(accessTokenRepository.findByTokenHashForUpdate(hash)).thenReturn(Optional.of(record));

        assertThrows(RefreshTokenRevokedException.class,
                () -> authCommandService.refresh(RefreshTokenCommand.builder().refreshToken(token).build()));
        verify(accessTokenRepository, never()).revokeAllByUserId(any(), any());
    }

    @Test
    void refresh_withinGrace_returnsCachedCredentials() {
        String token = "rotated-grace";
        String hash = "h-grace";
        UUID familyId = UUID.randomUUID();
        AccessToken record = AccessToken.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .tokenHash(hash)
                .tokenFamilyId(familyId)
                .deviceFingerprint("fp")
                .revokedAt(LocalDateTime.now().minusSeconds(3))
                .revokedReason(AccessToken.REASON_ROTATED)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now().minusDays(1))
                .version(0L)
                .build();
        when(accessTokenPort.hashToken(token)).thenReturn(hash);
        when(refreshTokenStore.findGraceCredentials(hash))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(new GraceCredentials("grace-access", "grace-refresh")));
        when(accessTokenPort.isTokenValid(token)).thenReturn(true);
        when(accessTokenPort.parseRefreshUserId(token)).thenReturn(USER_ID);
        when(accessTokenRepository.findByTokenHashForUpdate(hash)).thenReturn(Optional.of(record));
        when(authSecurityProperties.getRefreshReuseGrace()).thenReturn(Duration.ofSeconds(30));
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(activeUser));
        when(roleQueryService.getRoleNamesByUserId(USER_ID)).thenReturn(List.of("USER"));

        AuthView view = authCommandService.refresh(RefreshTokenCommand.builder().refreshToken(token).build());

        assertEquals("grace-access", view.accessToken());
        assertEquals("grace-refresh", view.refreshToken());
        verify(accessTokenPort, never()).issueAccessToken(any(), any());
    }

    @Test
    void refresh_graceHitAtStart_skipsDatabaseLookup() {
        String token = "old-refresh";
        String hash = "grace-start";
        when(accessTokenPort.hashToken(token)).thenReturn(hash);
        when(refreshTokenStore.findGraceCredentials(hash))
                .thenReturn(Optional.of(new GraceCredentials("cached-access", "cached-refresh")));
        when(accessTokenPort.parseRefreshUserId("cached-refresh")).thenReturn(USER_ID);
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(activeUser));
        when(roleQueryService.getRoleNamesByUserId(USER_ID)).thenReturn(List.of("USER"));

        AuthView view = authCommandService.refresh(RefreshTokenCommand.builder().refreshToken(token).build());

        assertEquals("cached-access", view.accessToken());
        verify(accessTokenRepository, never()).findByTokenHashForUpdate(any());
    }

    @Test
    void logout_denylistWriteFailure_propagates() {
        when(tokenTtlPort.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));
        doThrow(new SecurityDependencyUnavailableException("Security dependency temporarily unavailable"))
                .when(accessTokenRevocation).addToDenylist(eq("jti-1"), any());

        assertThrows(SecurityDependencyUnavailableException.class,
                () -> authCommandService.logout(USER_ID, Optional.empty(), Optional.of("jti-1")));
        verify(accessTokenRepository, never()).revokeByTokenHash(any(), any());
    }

    @Test
    void verifyAccount_otpRedisFailure_propagates() {
        when(otpStore.isMaxAttemptsExceeded("user@test.com", OtpType.EMAIL_VERIFY)).thenReturn(false);
        when(otpStore.find("user@test.com", OtpType.EMAIL_VERIFY))
                .thenThrow(new SecurityDependencyUnavailableException("Security dependency temporarily unavailable"));

        assertThrows(SecurityDependencyUnavailableException.class,
                () -> authCommandService.verifyAccount(new VerifyAccountCommand("user@test.com", "123456")));
        verify(userAccountPort, never()).findByEmail(anyString());
    }

    @Test
    void logoutAll_tokenVersionRedisSyncFailure_stillRevokesSessions() {
        when(userAccountPort.incrementTokenVersionById(USER_ID)).thenReturn(1);
        when(userAccountPort.findTokenVersionById(USER_ID)).thenReturn(Optional.of(2L));
        doThrow(new RuntimeException("redis down"))
                .when(accessTokenRevocation).setCachedTokenVersion(USER_ID, 2L);

        assertDoesNotThrow(() -> authCommandService.logoutAll(USER_ID));

        verify(accessTokenRepository).revokeAllByUserId(USER_ID, AccessToken.REASON_LOGOUT_ALL);
        verify(userAccountPort).incrementTokenVersionById(USER_ID);
    }

    @Test
    void refresh_graceWithoutCache_throwsUnavailableNotReuse() {
        String token = "rotated-retry";
        String hash = "h2";
        UUID familyId = UUID.randomUUID();
        AccessToken record = AccessToken.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .tokenHash(hash)
                .tokenFamilyId(familyId)
                .deviceFingerprint("fp")
                .ipAddress("127.0.0.1")
                .userAgent("ua")
                .revokedAt(LocalDateTime.now().minusSeconds(5))
                .revokedReason(AccessToken.REASON_ROTATED)
                .expiresAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now().minusDays(1))
                .version(0L)
                .build();
        when(accessTokenPort.hashToken(token)).thenReturn(hash);
        when(refreshTokenStore.findGraceCredentials(hash)).thenReturn(Optional.empty());
        when(accessTokenPort.isTokenValid(token)).thenReturn(true);
        when(accessTokenPort.parseRefreshUserId(token)).thenReturn(USER_ID);
        when(accessTokenRepository.findByTokenHashForUpdate(hash)).thenReturn(Optional.of(record));
        when(authSecurityProperties.getRefreshReuseGrace()).thenReturn(Duration.ofSeconds(30));

        assertThrows(RefreshUnavailableException.class,
                () -> authCommandService.refresh(RefreshTokenCommand.builder().refreshToken(token).build()));
        verify(accessTokenRepository, never()).revokeAllByUserId(any(), any());
    }

    @Test
    void refresh_tokenVersionChangedDuringRefresh_throwsSessionRevoked() {
        String oldRefresh = "old-refresh";
        String hash = "old-hash";
        UUID sessionId = UUID.randomUUID();
        AccessToken record = AccessToken.builder()
                .id(sessionId)
                .userId(USER_ID)
                .tokenHash(hash)
                .tokenFamilyId(sessionId)
                .deviceFingerprint("fp")
                .ipAddress("127.0.0.1")
                .userAgent("ua")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .version(0L)
                .build();
        activeUser.setTokenVersion(1L);
        User bumped = User.builder()
                .email(activeUser.getEmail())
                .username(activeUser.getUsername())
                .phoneNumber(activeUser.getPhoneNumber())
                .password(activeUser.getPassword())
                .status(UserStatus.ACTIVE)
                .build();
        bumped.setId(USER_ID);
        bumped.setTokenVersion(2L);

        when(accessTokenPort.hashToken(oldRefresh)).thenReturn(hash);
        when(refreshTokenStore.findGraceCredentials(hash)).thenReturn(Optional.empty());
        when(accessTokenPort.isTokenValid(oldRefresh)).thenReturn(true);
        when(accessTokenPort.parseRefreshUserId(oldRefresh)).thenReturn(USER_ID);
        when(accessTokenRepository.findByTokenHashForUpdate(hash)).thenReturn(Optional.of(record));
        when(userAccountPort.findById(USER_ID))
                .thenReturn(Optional.of(activeUser))
                .thenReturn(Optional.of(bumped));
        when(roleQueryService.getRoleNamesByUserId(USER_ID)).thenReturn(List.of("USER"));
        when(accessTokenRevocation.getDeviceAccessJti(USER_ID, "fp")).thenReturn(Optional.empty());

        assertThrows(SessionRevokedException.class,
                () -> authCommandService.refresh(RefreshTokenCommand.builder().refreshToken(oldRefresh).build()));
        verify(accessTokenPort, never()).issueAccessToken(any(), any());
    }

    @Test
    void refresh_accountDisabledDuringRefresh_throwsUserNotActive() {
        String oldRefresh = "old-refresh";
        String hash = "old-hash";
        UUID sessionId = UUID.randomUUID();
        AccessToken record = AccessToken.builder()
                .id(sessionId)
                .userId(USER_ID)
                .tokenHash(hash)
                .tokenFamilyId(sessionId)
                .deviceFingerprint("fp")
                .ipAddress("127.0.0.1")
                .userAgent("ua")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .version(0L)
                .build();
        activeUser.setStatus(UserStatus.INACTIVE);
        when(accessTokenPort.hashToken(oldRefresh)).thenReturn(hash);
        when(refreshTokenStore.findGraceCredentials(hash)).thenReturn(Optional.empty());
        when(accessTokenPort.isTokenValid(oldRefresh)).thenReturn(true);
        when(accessTokenPort.parseRefreshUserId(oldRefresh)).thenReturn(USER_ID);
        when(accessTokenRepository.findByTokenHashForUpdate(hash)).thenReturn(Optional.of(record));
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(activeUser));

        assertThrows(UserNotActiveException.class,
                () -> authCommandService.refresh(RefreshTokenCommand.builder().refreshToken(oldRefresh).build()));
        verify(accessTokenPort, never()).issueAccessToken(any(), any());
    }

    @Test
    void refresh_disabledAfterInitialActiveCheck_throwsUserNotActive() {
        String oldRefresh = "old-refresh-race";
        String hash = "old-hash-race";
        UUID sessionId = UUID.randomUUID();
        AccessToken record = AccessToken.builder()
                .id(sessionId)
                .userId(USER_ID)
                .tokenHash(hash)
                .tokenFamilyId(sessionId)
                .deviceFingerprint("fp")
                .ipAddress("127.0.0.1")
                .userAgent("ua")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .createdAt(LocalDateTime.now())
                .version(0L)
                .build();
        activeUser.setTokenVersion(1L);
        User disabled = User.builder()
                .email(activeUser.getEmail())
                .username(activeUser.getUsername())
                .phoneNumber(activeUser.getPhoneNumber())
                .password(activeUser.getPassword())
                .status(UserStatus.INACTIVE)
                .build();
        disabled.setId(USER_ID);
        disabled.setTokenVersion(1L);

        when(accessTokenPort.hashToken(oldRefresh)).thenReturn(hash);
        when(refreshTokenStore.findGraceCredentials(hash)).thenReturn(Optional.empty());
        when(accessTokenPort.isTokenValid(oldRefresh)).thenReturn(true);
        when(accessTokenPort.parseRefreshUserId(oldRefresh)).thenReturn(USER_ID);
        when(accessTokenRepository.findByTokenHashForUpdate(hash)).thenReturn(Optional.of(record));
        when(userAccountPort.findById(USER_ID))
                .thenReturn(Optional.of(activeUser))
                .thenReturn(Optional.of(disabled));
        when(roleQueryService.getRoleNamesByUserId(USER_ID)).thenReturn(List.of("USER"));
        when(accessTokenRevocation.getDeviceAccessJti(USER_ID, "fp")).thenReturn(Optional.empty());

        assertThrows(UserNotActiveException.class,
                () -> authCommandService.refresh(RefreshTokenCommand.builder().refreshToken(oldRefresh).build()));
        verify(accessTokenPort, never()).issueAccessToken(any(), any());
    }

    @Test
    void logout_revokesRefreshAndDenylistsAccessJti() {
        String refresh = "rt";
        String hash = "rh";
        AccessToken record = AccessToken.builder()
                .userId(USER_ID)
                .tokenHash(hash)
                .deviceFingerprint("fp")
                .build();
        when(accessTokenPort.hashToken(refresh)).thenReturn(hash);
        when(accessTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(record));
        when(tokenTtlPort.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));

        authCommandService.logout(USER_ID, Optional.of(refresh), Optional.of("jti-1"));

        verify(accessTokenRevocation).addToDenylist("jti-1", Duration.ofMinutes(15));
        verify(accessTokenRepository).revokeByTokenHash(hash, AccessToken.REASON_LOGOUT);
    }

    @Test
    void forgotPassword_queuesOtpForExistingUser() {
        when(userAccountPort.findByEmail("user@test.com")).thenReturn(Optional.of(activeUser));
        authCommandService.forgotPassword(ForgotPasswordCommand.builder().email("user@test.com").build());
        verify(otpStore).delete("user@test.com", OtpType.FORGOT_PASSWORD);
        verify(otpStore).save(eq("user@test.com"), eq(OtpType.FORGOT_PASSWORD), anyString());
        verify(otpStore).saveCooldown("user@test.com", OtpType.FORGOT_PASSWORD);
        verify(otpStore).incrementAttempts("user@test.com", OtpType.FORGOT_PASSWORD);
        verify(mailService).sendOtpEmail(eq("user@test.com"), anyString());
    }

    @Test
    void forgotPassword_onCooldown_isSilentlyIgnored() {
        when(otpStore.isOnCooldown("user@test.com", OtpType.FORGOT_PASSWORD)).thenReturn(true);

        authCommandService.forgotPassword(ForgotPasswordCommand.builder().email("user@test.com").build());

        verify(userAccountPort, never()).findByEmail(anyString());
        verify(mailService, never()).sendOtpEmail(anyString(), anyString());
    }

    @Test
    void forgotPassword_maxAttemptsExceeded_isSilentlyIgnored() {
        when(otpStore.isOnCooldown("user@test.com", OtpType.FORGOT_PASSWORD)).thenReturn(false);
        when(otpStore.isMaxAttemptsExceeded("user@test.com", OtpType.FORGOT_PASSWORD)).thenReturn(true);

        authCommandService.forgotPassword(ForgotPasswordCommand.builder().email("user@test.com").build());

        verify(userAccountPort, never()).findByEmail(anyString());
        verify(mailService, never()).sendOtpEmail(anyString(), anyString());
    }

    @Test
    void resetPassword_updatesPasswordAndRevokesTokens() {
        ResetPasswordCommand cmd = ResetPasswordCommand.builder()
                .email("user@test.com")
                .otp("123456")
                .newPassword("new-secret")
                .build();
        when(otpStore.find("user@test.com", OtpType.FORGOT_PASSWORD)).thenReturn(Optional.of("123456"));
        when(userAccountPort.findByEmail("user@test.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.encode("new-secret")).thenReturn("new-hash");
        when(userAccountPort.incrementTokenVersionById(USER_ID)).thenReturn(1);

        authCommandService.resetPassword(cmd);

        assertEquals("new-hash", activeUser.getPassword());
        verify(accessTokenRepository).revokeAllByUserId(USER_ID, AccessToken.REASON_PASSWORD_RESET);
        verify(refreshTokenStore).revokeAllForUser(USER_ID);
        verify(userAccountPort).incrementTokenVersionById(USER_ID);
        verify(otpStore).delete("user@test.com", OtpType.FORGOT_PASSWORD);
    }

    @Test
    void resetPassword_wrongOtp_throws() {
        when(otpStore.find("user@test.com", OtpType.FORGOT_PASSWORD)).thenReturn(Optional.of("111111"));
        ResetPasswordCommand cmd = ResetPasswordCommand.builder()
                .email("user@test.com")
                .otp("222222")
                .newPassword("x")
                .build();
        assertThrows(OtpInvalidException.class, () -> authCommandService.resetPassword(cmd));
        verify(otpStore).incrementAttempts("user@test.com", OtpType.FORGOT_PASSWORD);
    }

    @Test
    void login_afterReset_withOldPassword_fails() {
        activeUser.setPassword("new-hash");
        LoginCommand cmd = LoginCommand.builder()
                .identifier("user@test.com")
                .password("old-secret")
                .build();
        when(userAccountPort.findByEmail(cmd.getIdentifier())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("old-secret", "new-hash")).thenReturn(false);
        assertThrows(InvalidCredentialsException.class, () -> authCommandService.login(cmd));
    }

    @Test
    void resendVerificationOtp_unknownEmail_returnsWithoutSending() {
        when(userAccountPort.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() ->
                authCommandService.resendVerificationOtp(new ResendVerificationCommand("missing@test.com")));

        verify(mailService, never()).sendVerifyAccountOtp(anyString(), anyString(), anyString());
    }

    @Test
    void resendVerificationOtp_verifiedUser_returnsWithoutSending() {
        when(userAccountPort.findByEmail("user@test.com")).thenReturn(Optional.of(activeUser));

        assertDoesNotThrow(() ->
                authCommandService.resendVerificationOtp(new ResendVerificationCommand("user@test.com")));

        verify(mailService, never()).sendVerifyAccountOtp(anyString(), anyString(), anyString());
    }

    @Test
    void resendVerificationOtp_onCooldown_throws() {
        when(otpStore.isOnCooldown("p@test.com", OtpType.EMAIL_VERIFY)).thenReturn(true);
        when(otpStore.getCooldownTtlSeconds("p@test.com", OtpType.EMAIL_VERIFY)).thenReturn(30L);

        assertThrows(ResendCooldownException.class,
                () -> authCommandService.resendVerificationOtp(new ResendVerificationCommand("p@test.com")));
    }

    @Test
    void resendVerificationOtp_eligibleUser_sendsMailAndStoresOtp() {
        User pending = User.builder()
                .email("p@test.com")
                .username("p")
                .phoneNumber("+10000000002")
                .password("hash")
                .status(UserStatus.PENDING_VERIFIED)
                .build();
        pending.setId(USER_ID);
        when(userAccountPort.findByEmail("p@test.com")).thenReturn(Optional.of(pending));

        assertDoesNotThrow(() ->
                authCommandService.resendVerificationOtp(new ResendVerificationCommand("p@test.com")));

        verify(otpStore).delete("p@test.com", OtpType.EMAIL_VERIFY);
        verify(otpStore).save(eq("p@test.com"), eq(OtpType.EMAIL_VERIFY), anyString());
        verify(otpStore).saveCooldown("p@test.com", OtpType.EMAIL_VERIFY);
        verify(otpStore).incrementAttempts("p@test.com", OtpType.EMAIL_VERIFY);
        verify(mailService).sendVerifyAccountOtp(eq("p@test.com"), eq("p"), anyString());
    }

    @Test
    void resendForgotPasswordOtp_onCooldown_throws() {
        when(otpStore.isOnCooldown("user@test.com", OtpType.FORGOT_PASSWORD)).thenReturn(true);
        when(otpStore.getCooldownTtlSeconds("user@test.com", OtpType.FORGOT_PASSWORD)).thenReturn(30L);
        assertThrows(ResendCooldownException.class,
                () -> authCommandService.resendForgotPasswordOtp(new ResendOtpCommand("user@test.com")));
    }

    @Test
    void concurrentRegister_duplicateEmail_onlyOneSucceeds() throws Exception {
        RegisterCommand cmd = RegisterCommand.builder()
                .email("race@test.com")
                .username("race")
                .password("secret12")
                .build();
        AtomicInteger saves = new AtomicInteger();
        when(userAccountPort.existsByEmail(cmd.getEmail())).thenAnswer(inv -> saves.get() > 0);
        when(userAccountPort.existsByUsername(cmd.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userAccountPort.save(any(User.class))).thenAnswer(inv -> {
            if (saves.incrementAndGet() > 1) {
                throw new UserFieldAlreadyTakenException("email", cmd.getEmail());
            }
            User u = inv.getArgument(0);
            u.setId(USER_ID);
            return u;
        });

        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger failures = new AtomicInteger();
        Runnable task = () -> {
            try {
                start.await();
                authCommandService.register(cmd);
            } catch (UserFieldAlreadyTakenException e) {
                failures.incrementAndGet();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        };
        pool.submit(task);
        pool.submit(task);
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS));
        assertEquals(1, failures.get());
    }

    private ChangePasswordCommand changePasswordCmd(String current, String next, String confirm) {
        return ChangePasswordCommand.builder()
                .currentPassword(current)
                .newPassword(next)
                .confirmNewPassword(confirm)
                .ipAddress("127.0.0.1")
                .build();
    }

    @Test
    void changePassword_success_encodesPasswordRevokesSessionsAndAudits() {
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("old-secret", "encoded")).thenReturn(true);
        when(passwordEncoder.matches("new-secret1", "encoded")).thenReturn(false);
        when(passwordEncoder.encode("new-secret1")).thenReturn("new-hash");
        when(userAccountPort.incrementTokenVersionById(USER_ID)).thenReturn(1);
        when(userAccountPort.findTokenVersionById(USER_ID)).thenReturn(Optional.of(1L));

        authCommandService.changePassword(USER_ID, changePasswordCmd("old-secret", "new-secret1", "new-secret1"));

        assertEquals("new-hash", activeUser.getPassword());
        assertNotNull(activeUser.getPasswordUpdateAt());
        verify(userAccountPort).save(activeUser);
        verify(accessTokenRepository).revokeAllByUserId(USER_ID, AccessToken.REASON_PASSWORD_CHANGED);
        verify(refreshTokenStore).revokeAllForUser(USER_ID);
        verify(userAccountPort).incrementTokenVersionById(USER_ID);
        verify(accessTokenRevocation).setCachedTokenVersion(USER_ID, 1L);
        verify(auditLogService).log(
                eq(USER_ID),
                eq("users"),
                eq("PASSWORD_CHANGED"),
                isNull(),
                any(),
                eq("127.0.0.1")
        );
        verify(passwordEncoder).encode("new-secret1");
        verify(passwordEncoder, never()).encode("old-secret");
    }

    @Test
    void changePassword_usesAuthenticatedUserIdNotRequestIdentity() {
        UUID otherId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("old-secret", "encoded")).thenReturn(true);
        when(passwordEncoder.matches("new-secret1", "encoded")).thenReturn(false);
        when(passwordEncoder.encode("new-secret1")).thenReturn("new-hash");
        when(userAccountPort.incrementTokenVersionById(USER_ID)).thenReturn(1);

        authCommandService.changePassword(USER_ID, changePasswordCmd("old-secret", "new-secret1", "new-secret1"));

        verify(userAccountPort).findById(USER_ID);
        verify(userAccountPort, never()).findById(otherId);
        verify(accessTokenRepository).revokeAllByUserId(eq(USER_ID), anyString());
    }

    @Test
    void changePassword_currentPasswordIncorrect_rejectedWithoutRevocationOrAudit() {
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(CurrentPasswordIncorrectException.class, () ->
                authCommandService.changePassword(USER_ID, changePasswordCmd("wrong", "new-secret1", "new-secret1")));

        verify(userAccountPort, never()).save(any());
        verify(accessTokenRepository, never()).revokeAllByUserId(any(), anyString());
        verify(refreshTokenStore, never()).revokeAllForUser(any());
        verify(userAccountPort, never()).incrementTokenVersionById(any());
        verify(auditLogService, never()).log(any(), anyString(), eq("PASSWORD_CHANGED"), any(), any(), any());
    }

    @Test
    void changePassword_confirmationMismatch_rejected() {
        assertThrows(PasswordConfirmationMismatchException.class, () ->
                authCommandService.changePassword(USER_ID, changePasswordCmd("old-secret", "new-secret1", "other")));

        verify(userAccountPort, never()).findById(any());
        verify(auditLogService, never()).log(any(), anyString(), eq("PASSWORD_CHANGED"), any(), any(), any());
    }

    @Test
    void changePassword_sameAsCurrent_rejectedAfterVerification() {
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("same-pass", "encoded")).thenReturn(true);

        assertThrows(NewPasswordSameAsCurrentException.class, () ->
                authCommandService.changePassword(USER_ID, changePasswordCmd("same-pass", "same-pass", "same-pass")));

        verify(userAccountPort, never()).save(any());
        verify(accessTokenRepository, never()).revokeAllByUserId(any(), anyString());
        verify(auditLogService, never()).log(any(), anyString(), eq("PASSWORD_CHANGED"), any(), any(), any());
    }

    @Test
    void changePassword_weakPassword_rejected() {
        assertThrows(PasswordPolicyViolationException.class, () ->
                authCommandService.changePassword(USER_ID, changePasswordCmd("old-secret", "short", "short")));

        verify(userAccountPort, never()).findById(any());
        verify(auditLogService, never()).log(any(), anyString(), eq("PASSWORD_CHANGED"), any(), any(), any());
    }

    @Test
    void changePassword_inactiveUser_rejected() {
        activeUser.setStatus(UserStatus.SUSPENDED);
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(activeUser));

        assertThrows(UserNotActiveException.class, () ->
                authCommandService.changePassword(USER_ID, changePasswordCmd("old-secret", "new-secret1", "new-secret1")));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(userAccountPort, never()).save(any());
        verify(auditLogService, never()).log(any(), anyString(), eq("PASSWORD_CHANGED"), any(), any(), any());
    }

    @Test
    void changePassword_userNotFound_throws() {
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                authCommandService.changePassword(USER_ID, changePasswordCmd("old-secret", "new-secret1", "new-secret1")));
    }

    @Test
    void changePassword_tokenVersionBumpedExactlyOnce() {
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("old-secret", "encoded")).thenReturn(true);
        when(passwordEncoder.matches("new-secret1", "encoded")).thenReturn(false);
        when(passwordEncoder.encode("new-secret1")).thenReturn("new-hash");
        when(userAccountPort.incrementTokenVersionById(USER_ID)).thenReturn(1);

        authCommandService.changePassword(USER_ID, changePasswordCmd("old-secret", "new-secret1", "new-secret1"));

        verify(userAccountPort, times(1)).incrementTokenVersionById(USER_ID);
    }

    @Test
    void changePassword_concurrentSecondRequestFailsWhenCurrentNoLongerMatches() {
        when(userAccountPort.findById(USER_ID)).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("old-secret", "encoded")).thenReturn(true);
        when(passwordEncoder.matches("new-secret1", "encoded")).thenReturn(false);
        when(passwordEncoder.encode("new-secret1")).thenReturn("new-hash");
        when(userAccountPort.incrementTokenVersionById(USER_ID)).thenReturn(1);

        authCommandService.changePassword(USER_ID, changePasswordCmd("old-secret", "new-secret1", "new-secret1"));

        activeUser.setPassword("new-hash");
        when(passwordEncoder.matches("old-secret", "new-hash")).thenReturn(false);

        assertThrows(CurrentPasswordIncorrectException.class, () ->
                authCommandService.changePassword(USER_ID, changePasswordCmd("old-secret", "other-secret", "other-secret")));
    }

    @Test
    void changePassword_oldPasswordCannotLogin_newPasswordCan() {
        activeUser.setPassword("new-hash");
        when(userAccountPort.findByEmail("user@test.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("old-secret", "new-hash")).thenReturn(false);
        assertThrows(InvalidCredentialsException.class, () ->
                authCommandService.login(LoginCommand.builder()
                        .identifier("user@test.com")
                        .password("old-secret")
                        .build()));

        when(passwordEncoder.matches("new-secret1", "new-hash")).thenReturn(true);
        when(roleQueryService.getRoleNamesByUserId(USER_ID)).thenReturn(List.of("USER"));
        when(accessTokenPort.issueAccessToken(any(), any())).thenReturn(new IssuedAccessTokenView("a", "jti"));
        when(accessTokenPort.generateRefreshToken(USER_ID)).thenReturn("r");
        when(accessTokenPort.hashToken(anyString())).thenReturn("h");
        when(tokenTtlPort.getRefreshTokenTtl()).thenReturn(Duration.ofHours(1));

        assertDoesNotThrow(() -> authCommandService.login(LoginCommand.builder()
                .identifier("user@test.com")
                .password("new-secret1")
                .ipAddress("127.0.0.1")
                .userAgent("test")
                .build()));
    }

    @Test
    void resolveUserId_fromDomainUser() {
        assertEquals(USER_ID, authCommandService.resolveUserId(activeUser));
    }

    @Test
    void resolveUserId_missingPrincipal_throws() {
        assertThrows(IllegalStateException.class, () -> authCommandService.resolveUserId(null));
    }

    @Test
    void resolveUserId_unsupportedPrincipalType_throws() {
        UserDetails generic = org.springframework.security.core.userdetails.User
                .withUsername("x")
                .password("p")
                .roles("USER")
                .build();
        assertThrows(IllegalStateException.class, () -> authCommandService.resolveUserId(generic));
    }

    @Test
    void parseOptionalAccessJti_missingOrInvalidHeader_returnsEmpty() {
        assertTrue(authCommandService.parseOptionalAccessJti(null, USER_ID).isEmpty());
        assertTrue(authCommandService.parseOptionalAccessJti("Basic abc", USER_ID).isEmpty());
        when(accessTokenPort.parseAccessToken("bad")).thenThrow(new InvalidTokenException("bad"));
        assertTrue(authCommandService.parseOptionalAccessJti("Bearer bad", USER_ID).isEmpty());
    }

    @Test
    void parseOptionalAccessJti_validBearer_returnsJti() {
        when(accessTokenPort.parseAccessToken("good")).thenReturn(
                new ParsedAccessTokenView(USER_ID, "jti-99", 1L));
        assertEquals(Optional.of("jti-99"),
                authCommandService.parseOptionalAccessJti("Bearer good", USER_ID));
    }

    @Test
    void parseOptionalAccessJti_userMismatch_returnsEmpty() {
        UUID other = UUID.fromString("22222222-2222-2222-2222-222222222222");
        when(accessTokenPort.parseAccessToken("tok")).thenReturn(
                new ParsedAccessTokenView(other, "jti", 1L));
        assertTrue(authCommandService.parseOptionalAccessJti("Bearer tok", USER_ID).isEmpty());
    }

    @Test
    void logout_withoutRefreshToken_onlyDenylistsAccessJti() {
        when(tokenTtlPort.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));

        authCommandService.logout(USER_ID, Optional.empty(), Optional.of("jti-logout"));

        verify(accessTokenRevocation).addToDenylist(eq("jti-logout"), any());
        verify(accessTokenRepository, never()).revokeByTokenHash(any(), any());
    }
}
