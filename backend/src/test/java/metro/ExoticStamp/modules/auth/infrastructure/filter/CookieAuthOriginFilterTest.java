package metro.ExoticStamp.modules.auth.infrastructure.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import metro.ExoticStamp.config.CorsProperties;
import metro.ExoticStamp.modules.auth.config.AuthCookieProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import jakarta.servlet.http.Cookie;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CookieAuthOriginFilterTest {

    private CookieAuthOriginFilter filter;

    @BeforeEach
    void setUp() {
        CorsProperties corsProperties = new CorsProperties();
        corsProperties.setAllowedOrigins("http://localhost:5173,https://report.facewashfox.com");
        AuthCookieProperties cookieProperties = new AuthCookieProperties();
        cookieProperties.setName("refresh_token");
        filter = new CookieAuthOriginFilter(corsProperties, cookieProperties, new ObjectMapper());
    }

    @Test
    void cookieRefresh_allowedOrigin_passes() throws Exception {
        MockHttpServletRequest request = refreshWithCookie();
        request.addHeader("Origin", "http://localhost:5173");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertTrue(chain.getRequest() != null);
        assertEquals(200, response.getStatus() == 0 ? 200 : response.getStatus());
    }

    @Test
    void cookieRefresh_disallowedOrigin_forbidden() throws Exception {
        MockHttpServletRequest request = refreshWithCookie();
        request.addHeader("Origin", "https://evil.example");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("ORIGIN_FORBIDDEN"));
    }

    @Test
    void cookieRefresh_missingOriginAndReferer_rejected() throws Exception {
        MockHttpServletRequest request = refreshWithCookie();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("ORIGIN_REQUIRED"));
    }

    @Test
    void bodyRefresh_noOrigin_accepted() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/refresh");
        request.setContentType("application/json");
        request.setContent("{\"refreshToken\":\"native-refresh\"}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertTrue(chain.getRequest() != null);
    }

    @Test
    void cookieRefresh_allowedReferer_passes() throws Exception {
        MockHttpServletRequest request = refreshWithCookie();
        request.addHeader("Referer", "http://localhost:5173/dashboard");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertTrue(chain.getRequest() != null);
    }

    @Test
    void cookieRefresh_disallowedReferer_forbidden() throws Exception {
        MockHttpServletRequest request = refreshWithCookie();
        request.addHeader("Referer", "https://evil.example/page");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("ORIGIN_FORBIDDEN"));
    }

    @Test
    void login_withDisallowedOrigin_forbidden() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.addHeader("Origin", "https://evil.example");
        request.setContentType("application/json");
        request.setContent("{\"identifier\":\"a@b.c\",\"password\":\"x\"}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("ORIGIN_FORBIDDEN"));
    }

    @Test
    void login_withoutOrigin_passesForNativeClient() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setContentType("application/json");
        request.setContent("{\"identifier\":\"a@b.c\",\"password\":\"x\"}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertTrue(chain.getRequest() != null);
    }

    @Test
    void spoofedBodyHeader_withCookie_stillRequiresOrigin() throws Exception {
        MockHttpServletRequest request = refreshWithCookie();
        request.addHeader("X-Client-Transport", "body");
        request.setContentType("application/json");
        request.setContent("{}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("ORIGIN_REQUIRED"));
    }

    private MockHttpServletRequest refreshWithCookie() {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/refresh");
        request.setCookies(new Cookie("refresh_token", "cookie-refresh"));
        return request;
    }
}
