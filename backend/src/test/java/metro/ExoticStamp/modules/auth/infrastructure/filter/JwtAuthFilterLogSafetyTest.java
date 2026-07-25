package metro.ExoticStamp.modules.auth.infrastructure.filter;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidTokenException;
import metro.ExoticStamp.modules.auth.domain.exception.TokenExpiredException;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.ParsedAccessToken;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationStatus;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterLogSafetyTest {

    private static final String SECRET = "super-secret-access-token-value";

    @Mock private JwtProvider jwtProvider;
    @Mock private UserDetailsService userDetailsService;
    @Mock private RoleQueryService roleQueryService;
    @Mock private AccessTokenRevocationValidator accessTokenRevocationValidator;

    private ListAppender<ILoggingEvent> appender;
    private Logger logger;
    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(
                jwtProvider, userDetailsService, roleQueryService, accessTokenRevocationValidator,
                new ObjectMapper());
        logger = (Logger) LoggerFactory.getLogger(JwtAuthFilter.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        appender.stop();
        SecurityContextHolder.clearContext();
    }

    @Test
    void invalidTokenPath_doesNotLogRawBearerToken() throws Exception {
        when(jwtProvider.parseAccessToken(anyString()))
                .thenThrow(new InvalidTokenException("bad token"));

        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/me");
        request.addHeader("Authorization", "Bearer " + SECRET);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        String joined = appender.list.stream()
                .map(ILoggingEvent::getFormattedMessage)
                .reduce("", (a, b) -> a + "\n" + b);
        assertFalse(joined.contains(SECRET));
        assertFalse(joined.contains("Bearer " + SECRET));
    }
}

@ExtendWith(MockitoExtension.class)
class JwtAuthFilterTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String JTI = "jti-test-1";

    @Mock private JwtProvider jwtProvider;
    @Mock private UserDetailsService userDetailsService;
    @Mock private RoleQueryService roleQueryService;
    @Mock private AccessTokenRevocationValidator accessTokenRevocationValidator;

    private JwtAuthFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthFilter(
                jwtProvider, userDetailsService, roleQueryService, accessTokenRevocationValidator,
                new ObjectMapper());
    }

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void noAuthorizationHeader_passesThroughWithoutAuthentication() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/me");
        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(jwtProvider, never()).parseAccessToken(any());
    }

    @Test
    void denylistUnavailable_returns503WithRetryAfter() throws Exception {
        ParsedAccessToken parsed = new ParsedAccessToken(USER_ID, JTI, 1L);
        when(jwtProvider.parseAccessToken("valid-token")).thenReturn(parsed);
        when(accessTokenRevocationValidator.validate(USER_ID, JTI, 1L))
                .thenReturn(AccessTokenRevocationStatus.DEPENDENCY_UNAVAILABLE);

        MockHttpServletResponse response = doFilterWithBearer("valid-token");

        assertEquals(503, response.getStatus());
        assertEquals("5", response.getHeader("Retry-After"));
        assertTrue(response.getContentAsString().contains("SECURITY_DEPENDENCY_UNAVAILABLE"));
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void wrongTokenType_continuesWithoutAuthentication() throws Exception {
        when(jwtProvider.parseAccessToken("bad-type"))
                .thenThrow(new InvalidTokenException("Not an access token"));
        filter.doFilter(bearerRequest("bad-type"), new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void wrongIssuer_continuesWithoutAuthentication() throws Exception {
        when(jwtProvider.parseAccessToken("wrong-issuer"))
                .thenThrow(new InvalidTokenException("Invalid JWT issuer"));
        filter.doFilter(bearerRequest("wrong-issuer"), new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void malformedClaims_missingJti_continuesWithoutAuthentication() throws Exception {
        when(jwtProvider.parseAccessToken("no-jti"))
                .thenThrow(new InvalidTokenException("Missing jti"));
        filter.doFilter(bearerRequest("no-jti"), new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void expiredToken_continuesWithoutAuthentication() throws Exception {
        when(jwtProvider.parseAccessToken("expired"))
                .thenThrow(new TokenExpiredException("JWT expired"));
        filter.doFilter(bearerRequest("expired"), new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void revokedToken_doesNotSetAuthentication() throws Exception {
        ParsedAccessToken parsed = new ParsedAccessToken(USER_ID, JTI, 1L);
        when(jwtProvider.parseAccessToken("revoked")).thenReturn(parsed);
        when(accessTokenRevocationValidator.validate(USER_ID, JTI, 1L))
                .thenReturn(AccessTokenRevocationStatus.REVOKED);
        filter.doFilter(bearerRequest("revoked"), new MockHttpServletResponse(), new MockFilterChain());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userDetailsService, never()).loadUserByUsername(any());
    }

    @Test
    void validToken_setsAuthenticationWithRolesAndPermissions() throws Exception {
        ParsedAccessToken parsed = new ParsedAccessToken(USER_ID, JTI, 2L);
        when(jwtProvider.parseAccessToken("good")).thenReturn(parsed);
        when(accessTokenRevocationValidator.validate(USER_ID, JTI, 2L))
                .thenReturn(AccessTokenRevocationStatus.OK);
        when(userDetailsService.loadUserByUsername(USER_ID.toString()))
                .thenReturn(User.withUsername(USER_ID.toString()).password("n/a").roles("USER").build());
        when(roleQueryService.getRoleNamesByUserId(USER_ID)).thenReturn(List.of("ADMIN", "ROLE_USER"));
        when(roleQueryService.getPermissionCodesByUserId(USER_ID)).thenReturn(List.of("stamp:collect"));

        filter.doFilter(bearerRequest("good"), new MockHttpServletResponse(), new MockFilterChain());

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        assertTrue(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("stamp:collect")));
    }

    @Test
    void failOpenStatus_stillAuthenticates() throws Exception {
        ParsedAccessToken parsed = new ParsedAccessToken(USER_ID, JTI, 1L);
        when(jwtProvider.parseAccessToken("fail-open")).thenReturn(parsed);
        when(accessTokenRevocationValidator.validate(USER_ID, JTI, 1L))
                .thenReturn(AccessTokenRevocationStatus.FAIL_OPEN);
        when(userDetailsService.loadUserByUsername(USER_ID.toString()))
                .thenReturn(User.withUsername(USER_ID.toString()).password("n/a").roles("USER").build());
        when(roleQueryService.getRoleNamesByUserId(USER_ID)).thenReturn(List.of("USER"));
        when(roleQueryService.getPermissionCodesByUserId(USER_ID)).thenReturn(List.of());

        filter.doFilter(bearerRequest("fail-open"), new MockHttpServletResponse(), new MockFilterChain());

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(userDetailsService).loadUserByUsername(eq(USER_ID.toString()));
    }

    private MockHttpServletResponse doFilterWithBearer(String token) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(bearerRequest(token), response, new MockFilterChain());
        return response;
    }

    private static MockHttpServletRequest bearerRequest(String token) {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/me");
        request.addHeader("Authorization", "Bearer " + token);
        return request;
    }
}
