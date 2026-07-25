package metro.ExoticStamp.modules.auth.presentation.support;

import metro.ExoticStamp.modules.auth.domain.exception.ConflictingRefreshCredentialsException;
import metro.ExoticStamp.modules.auth.domain.model.AuthTransport;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthTransportResolverTest {

    @Test
    void transportFromRequest_readsHeader() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(AuthTransportResolver.TRANSPORT_HEADER, "cookie");
        assertEquals(AuthTransport.COOKIE, AuthTransportResolver.transportFromRequest(request));
    }

    @Test
    void resolveRefresh_bodyOnly() {
        var resolved = AuthTransportResolver.resolveRefreshCredential(
                Optional.empty(), Optional.of("body-token"));
        assertEquals("body-token", resolved.refreshToken());
        assertEquals(AuthTransport.BODY, resolved.transport());
    }

    @Test
    void resolveRefresh_cookieOnly() {
        var resolved = AuthTransportResolver.resolveRefreshCredential(
                Optional.of("cookie-token"), Optional.empty());
        assertEquals("cookie-token", resolved.refreshToken());
        assertEquals(AuthTransport.COOKIE, resolved.transport());
    }

    @Test
    void resolveRefresh_matchingCookieAndBody_prefersBodyTransport() {
        var resolved = AuthTransportResolver.resolveRefreshCredential(
                Optional.of("same"), Optional.of("same"));
        assertEquals("same", resolved.refreshToken());
        assertEquals(AuthTransport.BODY, resolved.transport());
    }

    @Test
    void resolveRefresh_conflictingTokens_throws() {
        assertThrows(ConflictingRefreshCredentialsException.class, () ->
                AuthTransportResolver.resolveRefreshCredential(
                        Optional.of("cookie"), Optional.of("body")));
    }

    @Test
    void resolveRefresh_nonePresent_returnsNullTokenWithCookieTransport() {
        var resolved = AuthTransportResolver.resolveRefreshCredential(Optional.empty(), Optional.empty());
        assertNull(resolved.refreshToken());
        assertEquals(AuthTransport.COOKIE, resolved.transport());
    }

    @Test
    void resolveRefresh_blankTokens_treatedAsAbsent() {
        var resolved = AuthTransportResolver.resolveRefreshCredential(
                Optional.of("   "), Optional.empty());
        assertNull(resolved.refreshToken());
        assertEquals(AuthTransport.COOKIE, resolved.transport());
    }
}
