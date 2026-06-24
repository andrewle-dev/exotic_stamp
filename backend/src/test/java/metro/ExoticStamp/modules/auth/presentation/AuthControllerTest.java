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
import metro.ExoticStamp.modules.auth.domain.exception.InvalidTokenException;
import metro.ExoticStamp.modules.auth.presentation.dto.request.LoginRequest;
import metro.ExoticStamp.modules.auth.presentation.mapper.AuthPresentationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.time.Duration;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({AuthWebMvcTestSecurityConfig.class, AuthPresentationMapper.class, GlobalExceptionHandler.class})
class AuthControllerTest {

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
                .andExpect(cookie().maxAge("refresh_token", 3600));
    }

    @Test
    void refresh_withoutCookie_returns401() throws Exception {
        when(commandService.refresh(any())).thenThrow(new InvalidTokenException("Refresh token missing"));

        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_TOKEN"));
    }
}
