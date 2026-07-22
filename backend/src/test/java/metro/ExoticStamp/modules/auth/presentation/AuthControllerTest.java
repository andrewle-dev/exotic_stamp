package metro.ExoticStamp.modules.auth.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import metro.ExoticStamp.modules.auth.AuthWebMvcTestSecurityConfig;
import metro.ExoticStamp.common.exceptions.GlobalExceptionHandler;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.auth.application.AuthCommandService;
import metro.ExoticStamp.modules.auth.application.port.TokenTtlPort;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.modules.auth.application.view.AuthUserView;
import metro.ExoticStamp.modules.auth.application.view.AuthView;
import metro.ExoticStamp.modules.auth.domain.exception.CurrentPasswordIncorrectException;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidTokenException;
import metro.ExoticStamp.modules.auth.presentation.dto.request.ChangePasswordRequest;
import metro.ExoticStamp.modules.auth.presentation.dto.request.LoginRequest;
import metro.ExoticStamp.modules.auth.config.AuthCookieProperties;
import metro.ExoticStamp.modules.auth.presentation.support.RefreshCookieSupport;
import metro.ExoticStamp.modules.auth.presentation.mapper.AuthPresentationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.time.Duration;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({
        AuthWebMvcTestSecurityConfig.class,
        AuthPresentationMapper.class,
        GlobalExceptionHandler.class,
        RefreshCookieSupport.class,
        AuthCookieProperties.class
})
class AuthControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthCommandService commandService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private AccessTokenRevocationValidator accessTokenRevocationValidator;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private CustomAuthEntryPoint authEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler accessDeniedHandler;

    @MockBean
    private RoleQueryService roleQueryService;

    @MockBean
    private TokenTtlPort tokenTtlPort;

    @Test
    void login_setsRefreshCookieAndOmitsRefreshTokenFromBody() throws Exception {
        when(tokenTtlPort.getRefreshTokenTtl()).thenReturn(Duration.ofHours(1));
        when(commandService.login(any())).thenReturn(new AuthView(
                "access-token",
                "refresh-token-value",
                new AuthUserView(java.util.UUID.randomUUID(), "u@test.com", "user1", List.of("USER"))
        ));

        LoginRequest req = new LoginRequest();
        req.setIdentifier("u@test.com");
        req.setPassword("secret");
        req.setDeviceFingerprint("12345678901234567890");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(cookie().exists("refresh_token"))
                .andExpect(cookie().path("refresh_token", "/api/v1/auth"))
                .andExpect(cookie().httpOnly("refresh_token", true))
                .andExpect(cookie().maxAge("refresh_token", 3600));
    }

    @Test
    void login_nativeTransport_includesRefreshInBody() throws Exception {
        when(tokenTtlPort.getRefreshTokenTtl()).thenReturn(Duration.ofHours(1));
        when(commandService.login(any())).thenReturn(new AuthView(
                "access-token",
                "refresh-token-value",
                new AuthUserView(java.util.UUID.randomUUID(), "u@test.com", "user1", List.of("USER"))
        ));

        LoginRequest req = new LoginRequest();
        req.setIdentifier("u@test.com");
        req.setPassword("secret");

        mockMvc.perform(post("/api/v1/auth/login")
                        .header("X-Client-Transport", "body")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token-value"));
    }

    @Test
    void refresh_withoutCookie_returns401() throws Exception {
        when(commandService.refresh(any())).thenThrow(new InvalidTokenException("Refresh token missing"));

        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }

    @Test
    void changePassword_unauthenticated_returns401() throws Exception {
        ChangePasswordRequest req = changePasswordRequest("CurrentPass1!", "NewSecurePass2!", "NewSecurePass2!");

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void changePassword_authenticatedUser_succeedsWithoutAdminRole() throws Exception {
        when(commandService.resolveUserId(any())).thenReturn(USER_ID);
        doNothing().when(commandService).changePassword(eq(USER_ID), any());

        ChangePasswordRequest req = changePasswordRequest("CurrentPass1!", "NewSecurePass2!", "NewSecurePass2!");

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Password changed successfully"))
                .andExpect(jsonPath("$.accessToken").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist())
                .andExpect(cookie().maxAge("refresh_token", 0));

        verify(commandService).changePassword(eq(USER_ID), any());
    }

    @Test
    @WithMockUser(roles = "USER")
    void changePassword_currentPasswordIncorrect_returns400() throws Exception {
        when(commandService.resolveUserId(any())).thenReturn(USER_ID);
        doThrow(new CurrentPasswordIncorrectException()).when(commandService).changePassword(eq(USER_ID), any());

        ChangePasswordRequest req = changePasswordRequest("WrongPass1!", "NewSecurePass2!", "NewSecurePass2!");

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CURRENT_PASSWORD_INCORRECT"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void changePassword_blankFields_returnsInvalidInput() throws Exception {
        when(commandService.resolveUserId(any())).thenReturn(USER_ID);

        ChangePasswordRequest req = changePasswordRequest("", "", "");

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT"));
    }

    private static ChangePasswordRequest changePasswordRequest(
            String current,
            String next,
            String confirm
    ) {
        ChangePasswordRequest req = new ChangePasswordRequest();
        req.setCurrentPassword(current);
        req.setNewPassword(next);
        req.setConfirmNewPassword(confirm);
        return req;
    }
}
