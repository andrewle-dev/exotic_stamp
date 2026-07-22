package metro.ExoticStamp.modules.auth.presentation;

import metro.ExoticStamp.modules.auth.application.port.TokenTtlPort;
import metro.ExoticStamp.modules.auth.config.AuthCookieProperties;
import metro.ExoticStamp.modules.auth.presentation.support.RefreshCookieSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefreshCookieSupportTest {

    @Mock
    private TokenTtlPort tokenTtlPort;

    private AuthCookieProperties props;
    private RefreshCookieSupport support;

    @BeforeEach
    void setUp() {
        props = new AuthCookieProperties();
        props.setName("refresh_token");
        props.setPath("/api/v1/auth");
        props.setSameSite("Lax");
        props.setSecureAlways(true);
        props.setHttpOnly(true);
        support = new RefreshCookieSupport(props, tokenTtlPort);
        lenient().when(tokenTtlPort.getRefreshTokenTtl()).thenReturn(Duration.ofHours(1));
    }

    @Test
    void setRefreshCookie_setsCurrentPathAndExpiresLegacyPath() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        support.setRefreshCookie(request, response, "token-value");

        List<String> headers = response.getHeaders("Set-Cookie");
        assertEquals(2, headers.size());
        assertTrue(headers.stream().anyMatch(h ->
                h.contains("refresh_token=token-value") && h.contains("Path=/api/v1/auth") && !h.contains("Path=/api/v1/auth/refresh")));
        assertTrue(headers.stream().anyMatch(h ->
                h.contains("Path=/api/v1/auth/refresh") && (h.contains("Max-Age=0") || h.contains("Max-Age=0;"))));
        assertTrue(headers.stream().allMatch(h -> h.contains("Secure") && h.contains("HttpOnly")));
    }

    @Test
    void clearRefreshCookie_expiresBothPaths() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        support.clearRefreshCookie(request, response);

        List<String> headers = response.getHeaders("Set-Cookie");
        assertEquals(2, headers.size());
        assertTrue(headers.stream().anyMatch(h -> h.contains("Path=/api/v1/auth") && h.contains("Max-Age=0")));
        assertTrue(headers.stream().anyMatch(h -> h.contains("Path=/api/v1/auth/refresh") && h.contains("Max-Age=0")));
    }
}
