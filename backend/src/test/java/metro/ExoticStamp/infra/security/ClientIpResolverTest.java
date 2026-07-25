package metro.ExoticStamp.infra.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.*;

class ClientIpResolverTest {

    private ClientIpResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new ClientIpResolver();
    }

    @Test
    void resolve_usesRemoteAddr_ignoresSpoofedXForwardedFor() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.5");
        request.addHeader("X-Forwarded-For", "203.0.113.9, 198.51.100.1");
        request.addHeader("Forwarded", "for=203.0.113.9");

        assertEquals("10.0.0.5", resolver.resolve(request));
    }

    @Test
    void resolve_blankRemoteAddr_returnsUnknown() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("   ");
        assertEquals(ClientIpResolver.UNKNOWN, resolver.resolve(request));
    }

    @Test
    void normalize_stripsIpv6Brackets() {
        assertEquals("2001:db8::1", resolver.normalize("[2001:db8::1]"));
    }

    @Test
    void normalize_stripsIpv6BracketsAndPort() {
        assertEquals("2001:db8::1", resolver.normalize("[2001:db8::1]:8443"));
    }

    @Test
    void normalize_doesNotBreakIpv6WithoutBrackets() {
        assertEquals("2001:db8::1", resolver.normalize("2001:db8::1"));
    }

    @Test
    void normalize_stripsIpv4Port() {
        assertEquals("192.168.1.10", resolver.normalize("192.168.1.10:443"));
    }

    @Test
    void normalize_blank_returnsUnknown() {
        assertEquals(ClientIpResolver.UNKNOWN, resolver.normalize(null));
        assertEquals(ClientIpResolver.UNKNOWN, resolver.normalize(""));
        assertEquals(ClientIpResolver.UNKNOWN, resolver.normalize("  "));
    }

    @Test
    void isLikelyIp_ipv4AndIpv6() {
        assertTrue(resolver.isLikelyIp("127.0.0.1"));
        assertTrue(resolver.isLikelyIp("2001:db8::1"));
        assertTrue(resolver.isLikelyIp("[2001:db8::1]"));
        assertFalse(resolver.isLikelyIp("not-an-ip"));
        assertFalse(resolver.isLikelyIp("999.999.999.999"));
        assertFalse(resolver.isLikelyIp(ClientIpResolver.UNKNOWN));
        assertFalse(resolver.isLikelyIp(null));
    }

    @Test
    void normalize_malformedDoesNotThrow() {
        assertDoesNotThrow(() -> resolver.normalize(":::"));
        assertDoesNotThrow(() -> resolver.normalize("1.2.3"));
        assertDoesNotThrow(() -> resolver.normalize("abc:def:ghi"));
    }
}
