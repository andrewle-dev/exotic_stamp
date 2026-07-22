package metro.ExoticStamp.modules.auth.infrastructure.filter;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidTokenException;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.userdetails.UserDetailsService;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyString;
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
                jwtProvider, userDetailsService, roleQueryService, accessTokenRevocationValidator);
        logger = (Logger) LoggerFactory.getLogger(JwtAuthFilter.class);
        appender = new ListAppender<>();
        appender.start();
        logger.addAppender(appender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(appender);
        appender.stop();
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
