package metro.ExoticStamp.common.exceptions;

import metro.ExoticStamp.modules.auth.domain.exception.InvalidCredentialsException;
import metro.ExoticStamp.modules.auth.domain.exception.ResendCooldownException;
import metro.ExoticStamp.modules.auth.domain.exception.SecurityBreachException;
import metro.ExoticStamp.modules.auth.domain.exception.UserNotActiveException;
import metro.ExoticStamp.modules.rbac.domain.exception.RoleAlreadyAssignedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = new MockHttpServletRequest("POST", "/api/v1/auth/login");
    }

    @Test
    void invalidCredentials_returnsStableErrorShape() {
        var response = handler.handleInvalidCredentials(new InvalidCredentialsException(), request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("INVALID_CREDENTIALS", response.getBody().code());
        assertEquals(401, response.getBody().status());
        assertNotNull(response.getBody().timestamp());
    }

    @Test
    void userNotActive_returns403() {
        var response = handler.handleUserNotActive(new UserNotActiveException(), request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("USER_NOT_ACTIVE", response.getBody().code());
    }

    @Test
    void resendCooldown_returns429() {
        var response = handler.handleResendCooldown(new ResendCooldownException(60), request);
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("RESEND_COOLDOWN", response.getBody().code());
    }

    @Test
    void securityBreach_returns401() {
        var response = handler.handleSecurityBreach(new SecurityBreachException("uid"), request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("SECURITY_BREACH", response.getBody().code());
    }

    @Test
    void roleAlreadyAssigned_returns409() {
        var response = handler.handleEmailTaken(
                new RoleAlreadyAssignedException(UUID.randomUUID(), "USER"), request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("ROLE_ALREADY_ASSIGNED", response.getBody().code());
    }

    @Test
    void genericException_doesNotExposeStackTrace() {
        var response = handler.handleAll(new RuntimeException("boom"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_ERROR", response.getBody().code());
        assertFalse(response.getBody().message().contains("boom"));
    }
}
