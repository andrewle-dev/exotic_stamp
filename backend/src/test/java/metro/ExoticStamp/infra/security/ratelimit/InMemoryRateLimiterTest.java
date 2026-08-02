package metro.ExoticStamp.infra.security.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import metro.ExoticStamp.common.exceptions.security.SecurityDependencyUnavailableException;
import metro.ExoticStamp.infra.security.ClientIpResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class InMemoryRateLimiterTest {

    private InMemoryRateLimiter limiter;
    private RateLimitProperties.Policy policy;

    @BeforeEach
    void setUp() {
        limiter = new InMemoryRateLimiter();
        policy = new RateLimitProperties.Policy();
        policy.setEnabled(true);
        policy.setCapacity(3);
        policy.setRefillTokens(3);
        policy.setRefillPeriod(Duration.ofMinutes(1));
        policy.setTtl(Duration.ofMinutes(5));
    }

    @Test
    void allowsUpToCapacityThenDenies() {
        assertTrue(limiter.tryConsume("rl:test:a", policy).allowed());
        assertTrue(limiter.tryConsume("rl:test:a", policy).allowed());
        assertTrue(limiter.tryConsume("rl:test:a", policy).allowed());

        RateLimitDecision denied = limiter.tryConsume("rl:test:a", policy);
        assertFalse(denied.allowed());
        assertTrue(denied.retryAfterSeconds() >= 1);
    }

    @Test
    void separateKeysAreIndependent() {
        assertTrue(limiter.tryConsume("rl:test:k1", policy).allowed());
        assertTrue(limiter.tryConsume("rl:test:k1", policy).allowed());
        assertTrue(limiter.tryConsume("rl:test:k1", policy).allowed());
        assertFalse(limiter.tryConsume("rl:test:k1", policy).allowed());

        assertTrue(limiter.tryConsume("rl:test:k2", policy).allowed());
    }

    @Test
    void capacityOne_secondRequestDeniedImmediately() {
        policy.setCapacity(1);
        policy.setRefillTokens(1);
        assertTrue(limiter.tryConsume("rl:burst", policy).allowed());
        RateLimitDecision d = limiter.tryConsume("rl:burst", policy);
        assertFalse(d.allowed());
        assertTrue(d.retryAfterSeconds() >= 1);
    }

    @Test
    void clear_resetsBuckets() {
        policy.setCapacity(1);
        assertTrue(limiter.tryConsume("rl:clear", policy).allowed());
        assertFalse(limiter.tryConsume("rl:clear", policy).allowed());
        limiter.clear();
        assertTrue(limiter.tryConsume("rl:clear", policy).allowed());
    }
}

@ExtendWith(MockitoExtension.class)
class RateLimitFilterTest {

    @Mock private RateLimitService rateLimitService;
    @Mock private RateLimitKeyHasher keyHasher;
    @Mock private ClientIpResolver clientIpResolver;

    private RateLimitProperties properties;
    private RateLimitFilter filter;

    @BeforeEach
    void setUpFilter() {
        properties = new RateLimitProperties();
        properties.setEnabled(true);
        properties.setKeyPepper("test-pepper-at-least-32-chars-long!!");
        filter = new RateLimitFilter(
                properties, rateLimitService, keyHasher, clientIpResolver, new ObjectMapper().registerModule(new JavaTimeModule()));
    }

    @Test
    void redisFailure_returns503SecurityDependencyUnavailable() throws Exception {
        when(clientIpResolver.resolve(any())).thenReturn("127.0.0.1");
        when(keyHasher.buildKey(eq(RateLimitPolicyName.LOGIN), eq("127.0.0.1"), any()))
                .thenReturn("rl:login:hash");
        doThrow(new SecurityDependencyUnavailableException("Security dependency temporarily unavailable"))
                .when(rateLimitService).tryConsume(eq(RateLimitPolicyName.LOGIN), eq("rl:login:hash"));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
        request.setContentType("application/json");
        request.setContent("{\"identifier\":\"a@b.c\",\"password\":\"secret12\"}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        assertEquals(503, response.getStatus());
        assertEquals("5", response.getHeader("Retry-After"));
        assertTrue(response.getContentAsString().contains("SECURITY_DEPENDENCY_UNAVAILABLE"));
        assertNull(chain.getRequest());
    }

    @Test
    void rateLimitExceeded_returns429() throws Exception {
        when(clientIpResolver.resolve(any())).thenReturn("127.0.0.1");
        when(keyHasher.buildKey(eq(RateLimitPolicyName.REGISTER), eq("127.0.0.1"), any()))
                .thenReturn("rl:register:hash");
        doThrow(new RateLimitExceededException(30))
                .when(rateLimitService).tryConsume(eq(RateLimitPolicyName.REGISTER), eq("rl:register:hash"));

        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/v1/auth/register");
        request.setContentType("application/json");
        request.setContent("{\"email\":\"a@b.c\",\"username\":\"u\",\"password\":\"secret12\"}".getBytes());
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(429, response.getStatus());
        assertEquals("30", response.getHeader("Retry-After"));
        assertTrue(response.getContentAsString().contains("RATE_LIMIT_EXCEEDED"));
    }

    @Test
    void nonRateLimitedPath_passesThrough() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/health");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        verify(rateLimitService, never()).tryConsume(any(), any());
        assertNotNull(chain.getRequest());
    }
}
