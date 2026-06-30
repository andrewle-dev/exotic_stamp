package metro.ExoticStamp.modules.auth.presentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import metro.ExoticStamp.common.exceptions.GlobalExceptionHandler;
import metro.ExoticStamp.infra.mail.MailService;
import metro.ExoticStamp.modules.auth.AuthWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.auth.application.AuditLogService;
import metro.ExoticStamp.modules.auth.application.AuthCommandService;
import metro.ExoticStamp.modules.auth.application.port.AccessTokenPort;
import metro.ExoticStamp.modules.auth.application.port.AccessTokenRevocationPort;
import metro.ExoticStamp.modules.auth.application.port.OtpStorePort;
import metro.ExoticStamp.modules.auth.application.port.RefreshTokenStorePort;
import metro.ExoticStamp.modules.auth.application.port.TokenTtlPort;
import metro.ExoticStamp.modules.auth.config.AuthSecurityProperties;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.auth.presentation.dto.request.ResendVerificationRequest;
import metro.ExoticStamp.modules.auth.presentation.mapper.AuthPresentationMapper;
import metro.ExoticStamp.modules.auth.domain.repository.AccessTokenRepository;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.model.UserStatus;
import metro.ExoticStamp.modules.user.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({
        AuthWebMvcTestSecurityConfig.class,
        AuthPresentationMapper.class,
        GlobalExceptionHandler.class,
        AuthCommandService.class
})
class AuthResendVerificationControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String GENERIC_RESEND_MESSAGE =
            "If the account exists and is eligible, a verification code has been sent.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean private UserRepository userRepository;
    @MockBean private AccessTokenRepository accessTokenRepository;
    @MockBean private RoleQueryService roleQueryService;
    @MockBean private AccessTokenPort accessTokenPort;
    @MockBean private RefreshTokenStorePort refreshTokenStore;
    @MockBean private AccessTokenRevocationPort accessTokenRevocation;
    @MockBean private OtpStorePort otpStore;
    @MockBean private PasswordEncoder passwordEncoder;
    @MockBean private AuditLogService auditLogService;
    @MockBean private ApplicationEventPublisher eventPublisher;
    @MockBean private TokenTtlPort tokenTtlPort;
    @MockBean private MailService mailService;
    @MockBean private AuthSecurityProperties authSecurityProperties;

    @MockBean private JwtProvider jwtProvider;
    @MockBean private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private CustomAuthEntryPoint authEntryPoint;
    @MockBean private CustomAccessDeniedHandler accessDeniedHandler;

    @BeforeEach
    void setUp() {
        when(tokenTtlPort.getRefreshTokenTtl()).thenReturn(Duration.ofDays(7));
        AuthSecurityProperties.Otp otp = new AuthSecurityProperties.Otp();
        when(authSecurityProperties.getOtp()).thenReturn(otp);
    }

    @Test
    void resendVerificationOtp_publicResponses_areIndistinguishableAcrossStates() throws Exception {
        when(userRepository.findByEmail("missing@test.com")).thenReturn(Optional.empty());
        MvcResult unknown = performResend("missing@test.com");
        verify(mailService, never()).sendVerifyAccountOtp(anyString(), anyString(), anyString());

        clearInvocations(otpStore, mailService);
        User verified = user("verified@test.com", "verified", UserStatus.ACTIVE);
        when(userRepository.findByEmail("verified@test.com")).thenReturn(Optional.of(verified));
        MvcResult verifiedResult = performResend("verified@test.com");
        verify(mailService, never()).sendVerifyAccountOtp(anyString(), anyString(), anyString());

        clearInvocations(otpStore, mailService);
        User eligible = user("eligible@test.com", "eligible", UserStatus.PENDING_VERIFIED);
        when(userRepository.findByEmail("eligible@test.com")).thenReturn(Optional.of(eligible));
        MvcResult eligibleResult = performResend("eligible@test.com");
        verify(otpStore).delete("eligible@test.com", metro.ExoticStamp.modules.auth.domain.model.OtpType.EMAIL_VERIFY);
        verify(mailService).sendVerifyAccountOtp(eq("eligible@test.com"), eq("eligible"), anyString());

        assertPublicEnvelopeEquals(unknown, verifiedResult);
        assertPublicEnvelopeEquals(verifiedResult, eligibleResult);
        assertPublicResendBody(unknown);
        assertPublicResendBody(verifiedResult);
        assertPublicResendBody(eligibleResult);
    }

    @Test
    void resendVerificationOtp_onCooldown_returns429() throws Exception {
        when(otpStore.isOnCooldown("throttled@test.com", metro.ExoticStamp.modules.auth.domain.model.OtpType.EMAIL_VERIFY))
                .thenReturn(true);
        when(otpStore.getCooldownTtlSeconds("throttled@test.com", metro.ExoticStamp.modules.auth.domain.model.OtpType.EMAIL_VERIFY))
                .thenReturn(45L);

        ResendVerificationRequest req = new ResendVerificationRequest();
        req.setEmail("throttled@test.com");

        mockMvc.perform(post("/api/v1/auth/resend-verification-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("RESEND_COOLDOWN"));
    }

    private void assertPublicEnvelopeEquals(MvcResult left, MvcResult right) throws Exception {
        JsonNode leftNode = objectMapper.readTree(left.getResponse().getContentAsString());
        JsonNode rightNode = objectMapper.readTree(right.getResponse().getContentAsString());
        assertThat(leftNode.get("success")).isEqualTo(rightNode.get("success"));
        assertThat(leftNode.get("message")).isEqualTo(rightNode.get("message"));
        assertThat(leftNode.get("data")).isEqualTo(rightNode.get("data"));
        assertThat(leftNode.has("timestamp")).isTrue();
        assertThat(rightNode.has("timestamp")).isTrue();
    }

    private void assertPublicResendBody(MvcResult result) throws Exception {
        String body = result.getResponse().getContentAsString();
        assertThat(body).contains("\"success\":true");
        assertThat(body).contains("\"message\":\"" + GENERIC_RESEND_MESSAGE + "\"");
        assertThat(body).contains("\"data\":null");
        assertThat(body).contains("\"timestamp\":");
    }

    private MvcResult performResend(String email) throws Exception {
        ResendVerificationRequest req = new ResendVerificationRequest();
        req.setEmail(email);

        return mockMvc.perform(post("/api/v1/auth/resend-verification-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value(GENERIC_RESEND_MESSAGE))
                .andExpect(jsonPath("$.data").isEmpty())
                .andReturn();
    }

    private static User user(String email, String username, UserStatus status) {
        User user = User.builder()
                .email(email)
                .username(username)
                .phoneNumber("+10000000009")
                .password("encoded")
                .status(status)
                .build();
        user.setId(USER_ID);
        return user;
    }
}
