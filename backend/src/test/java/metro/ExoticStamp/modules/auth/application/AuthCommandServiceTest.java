package metro.ExoticStamp.modules.auth.application;

import metro.ExoticStamp.infra.mail.MailProperties;
import metro.ExoticStamp.infra.mail.MailService;
import metro.ExoticStamp.modules.auth.application.command.ForgotPasswordCommand;
import metro.ExoticStamp.modules.auth.application.command.LoginCommand;
import metro.ExoticStamp.modules.auth.application.command.RefreshTokenCommand;
import metro.ExoticStamp.modules.auth.application.command.RegisterCommand;
import metro.ExoticStamp.modules.auth.application.command.ResendOtpCommand;
import metro.ExoticStamp.modules.auth.application.command.ResendVerificationCommand;
import metro.ExoticStamp.modules.auth.application.command.ResetPasswordCommand;
import metro.ExoticStamp.modules.auth.application.command.VerifyTokenCommand;
import metro.ExoticStamp.modules.auth.application.port.AccessTokenPort;
import metro.ExoticStamp.modules.auth.application.port.AccessTokenRevocationPort;
import metro.ExoticStamp.modules.auth.application.port.EmailVerificationTokenPort;
import metro.ExoticStamp.modules.auth.application.port.OtpStorePort;
import metro.ExoticStamp.modules.auth.application.port.RefreshTokenStorePort;
import metro.ExoticStamp.modules.auth.application.port.TokenTtlPort;
import metro.ExoticStamp.modules.auth.application.view.AuthView;
import metro.ExoticStamp.modules.auth.application.view.IssuedAccessTokenView;
import metro.ExoticStamp.modules.auth.config.AuthSecurityProperties;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidCredentialsException;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidTokenException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpInvalidException;
import metro.ExoticStamp.modules.auth.domain.exception.ResendCooldownException;
import metro.ExoticStamp.modules.auth.domain.exception.SecurityBreachException;
import metro.ExoticStamp.modules.auth.domain.exception.UserNotActiveException;
import metro.ExoticStamp.modules.auth.domain.model.AccessToken;
import metro.ExoticStamp.modules.auth.domain.model.OtpType;
import metro.ExoticStamp.modules.auth.domain.repository.AccessTokenRepository;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.modules.user.domain.exception.UserFieldAlreadyTakenException;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.model.UserStatus;
import metro.ExoticStamp.modules.user.domain.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
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
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class AuthCommandServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Mock private UserRepository userRepository;
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
    @Mock private EmailVerificationTokenPort emailVerificationTokenPort;
    @Mock private MailService mailService;
    @Mock private MailProperties mailProperties;
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
    }

    @AfterEach
    void clearTransactionSync() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clear();
        }
    }

    @Test
    void register_success_savesUserAndVerificationToken() {
        RegisterCommand cmd = RegisterCommand.builder()
                .email("new@test.com")
                .username("newuser")
                .password("secret")
                .firstname("A")
                .lastname("B")
                .phoneNumber("+10000000001")
                .build();
        when(userRepository.existsByEmail(cmd.getEmail())).thenReturn(false);
        when(userRepository.existsByUsername(cmd.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(cmd.getPassword())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(USER_ID);
            return u;
        });

        authCommandService.register(cmd);

        verify(emailVerificationTokenPort).saveToken(anyString(), eq(USER_ID));
        verify(userRepository).save(argThat(u -> u.getStatus() == UserStatus.PENDING_VERIFIED));
    }

    @Test
    void register_duplicateEmail_throws() {
        when(userRepository.existsByEmail("dup@test.com")).thenReturn(true);
        RegisterCommand cmd = RegisterCommand.builder()
                .email("dup@test.com")
                .username("u")
                .password("p")
                .build();
        assertThrows(UserFieldAlreadyTakenException.class, () -> authCommandService.register(cmd));
    }

    @Test
    void register_duplicateUsername_throws() {
        when(userRepository.existsByEmail("a@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("taken")).thenReturn(true);
        RegisterCommand cmd = RegisterCommand.builder()
                .email("a@test.com")
                .username("taken")
                .password("p")
                .build();
        assertThrows(UserFieldAlreadyTakenException.class, () -> authCommandService.register(cmd));
    }

    @Test
    void register_duplicatePhone_throws() {
        when(userRepository.existsByEmail("a@test.com")).thenReturn(false);
        when(userRepository.existsByUsername("user")).thenReturn(false);
        when(userRepository.existsByPhoneNumber("+10000000009")).thenReturn(true);
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
        when(userRepository.findByEmail(cmd.getIdentifier())).thenReturn(Optional.of(activeUser));
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
        when(userRepository.findByEmail(cmd.getIdentifier())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(cmd.getPassword(), activeUser.getPassword())).thenReturn(false);
        assertThrows(InvalidCredentialsException.class, () -> authCommandService.login(cmd));
    }

    @Test
    void login_unverifiedUser_throws() {
        activeUser.setStatus(UserStatus.PENDING_VERIFIED);
        LoginCommand cmd = LoginCommand.builder()
                .identifier("user@test.com")
                .password("secret")
                .build();
        when(userRepository.findByEmail(cmd.getIdentifier())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches(cmd.getPassword(), activeUser.getPassword())).thenReturn(true);
        assertThrows(UserNotActiveException.class, () -> authCommandService.login(cmd));
    }

    @Test
    void verifyEmail_success_activatesUser() {
        String token = "verify-token";
        when(emailVerificationTokenPort.findUserIdByToken(token)).thenReturn(Optional.of(USER_ID));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(activeUser));
        activeUser.setStatus(UserStatus.PENDING_VERIFIED);

        authCommandService.verifyEmail(new VerifyTokenCommand(token));

        assertEquals(UserStatus.ACTIVE, activeUser.getStatus());
        assertNotNull(activeUser.getVerifiedAt());
        verify(emailVerificationTokenPort).deleteToken(token);
    }

    @Test
    void verifyEmail_invalidToken_throws() {
        when(emailVerificationTokenPort.findUserIdByToken("bad")).thenReturn(Optional.empty());
        assertThrows(InvalidTokenException.class,
                () -> authCommandService.verifyEmail(new VerifyTokenCommand("bad")));
    }

    @Test
    void refresh_success_rotatesTokens() {
        String oldRefresh = "old-refresh";
        String hash = "old-hash";
        AccessToken record = AccessToken.builder()
                .id(UUID.randomUUID())
                .userId(USER_ID)
                .tokenHash(hash)
                .deviceFingerprint("fp")
                .expiresAt(LocalDateTime.now().plusDays(1))
                .build();
        when(accessTokenPort.hashToken(oldRefresh)).thenReturn(hash);
        when(refreshTokenStore.isRevoked(hash)).thenReturn(false);
        when(accessTokenPort.isTokenValid(oldRefresh)).thenReturn(true);
        when(accessTokenPort.extractUserId(oldRefresh)).thenReturn(USER_ID);
        when(accessTokenRepository.findByTokenHash(hash)).thenReturn(Optional.of(record));
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(activeUser));
        when(roleQueryService.getRoleNamesByUserId(USER_ID)).thenReturn(List.of("USER"));
        when(accessTokenRevocation.getDeviceAccessJti(USER_ID, "fp")).thenReturn(Optional.of("old-jti"));
        when(accessTokenPort.issueAccessToken(eq(activeUser), anyList()))
                .thenReturn(new IssuedAccessTokenView("new-access", "new-jti"));
        when(accessTokenPort.generateRefreshToken(USER_ID)).thenReturn("new-refresh");
        when(accessTokenPort.hashToken("new-refresh")).thenReturn("new-hash");
        when(tokenTtlPort.getRefreshTokenTtl()).thenReturn(Duration.ofDays(7));
        when(tokenTtlPort.getAccessTokenTtl()).thenReturn(Duration.ofMinutes(15));

        AuthView view = authCommandService.refresh(new RefreshTokenCommand(oldRefresh));

        assertEquals("new-access", view.accessToken());
        verify(accessTokenRepository).revokeByTokenHash(hash, AccessToken.REASON_ROTATED);
        verify(refreshTokenStore).revoke(USER_ID, "fp", hash);
    }

    @Test
    void refresh_reuseAttack_throwsAndRevokesAll() {
        String token = "reused";
        String hash = "h";
        when(accessTokenPort.hashToken(token)).thenReturn(hash);
        when(refreshTokenStore.isRevoked(hash)).thenReturn(true);
        when(accessTokenPort.extractUserId(token)).thenReturn(USER_ID);

        assertThrows(SecurityBreachException.class,
                () -> authCommandService.refresh(new RefreshTokenCommand(token)));
        verify(accessTokenRepository).revokeAllByUserId(USER_ID, AccessToken.REASON_REUSE_ATTACK);
        verify(refreshTokenStore).revokeAllForUser(USER_ID);
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
        verify(refreshTokenStore).revoke(USER_ID, "fp", hash);
    }

    @Test
    void forgotPassword_queuesOtpForExistingUser() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(activeUser));
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

        verify(userRepository, never()).findByEmail(anyString());
        verify(mailService, never()).sendOtpEmail(anyString(), anyString());
    }

    @Test
    void forgotPassword_maxAttemptsExceeded_isSilentlyIgnored() {
        when(otpStore.isOnCooldown("user@test.com", OtpType.FORGOT_PASSWORD)).thenReturn(false);
        when(otpStore.isMaxAttemptsExceeded("user@test.com", OtpType.FORGOT_PASSWORD)).thenReturn(true);

        authCommandService.forgotPassword(ForgotPasswordCommand.builder().email("user@test.com").build());

        verify(userRepository, never()).findByEmail(anyString());
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
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.encode("new-secret")).thenReturn("new-hash");
        when(userRepository.incrementTokenVersionById(USER_ID)).thenReturn(1);

        authCommandService.resetPassword(cmd);

        assertEquals("new-hash", activeUser.getPassword());
        verify(accessTokenRepository).revokeAllByUserId(USER_ID, AccessToken.REASON_PASSWORD_RESET);
        verify(refreshTokenStore).revokeAllForUser(USER_ID);
        verify(userRepository).incrementTokenVersionById(USER_ID);
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
    }

    @Test
    void login_afterReset_withOldPassword_fails() {
        activeUser.setPassword("new-hash");
        LoginCommand cmd = LoginCommand.builder()
                .identifier("user@test.com")
                .password("old-secret")
                .build();
        when(userRepository.findByEmail(cmd.getIdentifier())).thenReturn(Optional.of(activeUser));
        when(passwordEncoder.matches("old-secret", "new-hash")).thenReturn(false);
        assertThrows(InvalidCredentialsException.class, () -> authCommandService.login(cmd));
    }

    @Test
    void resendVerification_unknownEmail_isSilentlyIgnored() {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());

        assertDoesNotThrow(() ->
                authCommandService.resendVerification(new ResendVerificationCommand("missing@test.com")));

        verify(emailVerificationTokenPort, never()).saveToken(anyString(), any());
        verify(mailService, never()).sendVerifyEmail(anyString(), anyString(), anyString());
    }

    @Test
    void resendVerification_verifiedUser_isSilentlyIgnored() {
        when(userRepository.findByEmail("user@test.com")).thenReturn(Optional.of(activeUser));

        assertDoesNotThrow(() ->
                authCommandService.resendVerification(new ResendVerificationCommand("user@test.com")));

        verify(emailVerificationTokenPort, never()).saveToken(anyString(), any());
        verify(mailService, never()).sendVerifyEmail(anyString(), anyString(), anyString());
    }

    @Test
    void resendVerification_onCooldown_isSilentlyIgnored() {
        User pending = User.builder()
                .email("p@test.com")
                .username("p")
                .phoneNumber("+10000000002")
                .password("hash")
                .status(UserStatus.PENDING_VERIFIED)
                .build();
        pending.setId(USER_ID);
        when(userRepository.findByEmail("p@test.com")).thenReturn(Optional.of(pending));
        when(emailVerificationTokenPort.isOnCooldown("p@test.com")).thenReturn(true);

        assertDoesNotThrow(() ->
                authCommandService.resendVerification(new ResendVerificationCommand("p@test.com")));

        verify(emailVerificationTokenPort, never()).saveToken(anyString(), any());
        verify(mailService, never()).sendVerifyEmail(anyString(), anyString(), anyString());
    }

    @Test
    void resendVerification_eligibleUser_sendsMailAndStoresToken() {
        User pending = User.builder()
                .email("p@test.com")
                .username("p")
                .phoneNumber("+10000000002")
                .password("hash")
                .status(UserStatus.PENDING_VERIFIED)
                .build();
        pending.setId(USER_ID);
        when(userRepository.findByEmail("p@test.com")).thenReturn(Optional.of(pending));
        when(mailProperties.getFrontendUrl()).thenReturn("http://localhost:3000");

        assertDoesNotThrow(() ->
                authCommandService.resendVerification(new ResendVerificationCommand("p@test.com")));

        verify(emailVerificationTokenPort).saveToken(anyString(), eq(USER_ID));
        verify(emailVerificationTokenPort).saveCooldown("p@test.com");
        verify(mailService).sendVerifyEmail(eq("p@test.com"), eq("p"), contains("/verify-email?token="));
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
                .password("p")
                .build();
        AtomicInteger saves = new AtomicInteger();
        when(userRepository.existsByEmail(cmd.getEmail())).thenAnswer(inv -> saves.get() > 0);
        when(userRepository.existsByUsername(cmd.getUsername())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hash");
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
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
}
