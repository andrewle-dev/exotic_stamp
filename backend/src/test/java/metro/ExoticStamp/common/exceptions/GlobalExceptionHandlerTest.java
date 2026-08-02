package metro.ExoticStamp.common.exceptions;

import metro.ExoticStamp.modules.collection.domain.exception.IdempotencyConflictException;
import metro.ExoticStamp.modules.auth.domain.exception.AccountNotVerifiedException;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidCredentialsException;
import metro.ExoticStamp.modules.auth.domain.exception.ResendCooldownException;
import metro.ExoticStamp.modules.auth.domain.exception.SecurityBreachException;
import metro.ExoticStamp.modules.auth.domain.exception.UserNotActiveException;
import metro.ExoticStamp.modules.rbac.domain.exception.RoleAlreadyAssignedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
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
    void unknownUniqueViolation_doesNotLeakConstraintName() {
        request = new MockHttpServletRequest("POST", "/api/v1/collections/scan");
        String pg = "ERROR: duplicate key value violates unique constraint \"uq_user_stamps_user_idempotency\"";
        var response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("could not execute statement", new RuntimeException(pg)),
                request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("DATA_INTEGRITY_VIOLATION", response.getBody().code());
        assertEquals("Duplicate or conflicting data", response.getBody().message());
        assertFalse(response.getBody().message().contains("uq_user_stamps"));
    }

    @Test
    void idempotencyConflict_returns409WithoutConstraintDetails() {
        request = new MockHttpServletRequest("POST", "/api/v1/collections/scan");
        var response = handler.handleIdempotencyLogicalConflict(new IdempotencyConflictException(), request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("IDEMPOTENCY_CONFLICT", response.getBody().code());
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
    void accountNotVerified_returns403() {
        var response = handler.handleAccountNotVerified(new AccountNotVerifiedException(), request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("ACCOUNT_NOT_VERIFIED", response.getBody().code());
    }

    @Test
    void userNotActive_returns403() {
        var response = handler.handleUserNotActive(new UserNotActiveException(), request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("ACCOUNT_DISABLED", response.getBody().code());
    }

    @Test
    void resendCooldown_returns429() {
        var response = handler.handleResendCooldown(new ResendCooldownException(60), request);
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals("RESEND_COOLDOWN", response.getBody().code());
    }

    @Test
    void securityBreach_returns401MappedToRefreshReuse() {
        var response = handler.handleSecurityBreach(new SecurityBreachException("uid"), request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("REFRESH_TOKEN_REUSED", response.getBody().code());
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

    @Test
    void foreignKeyViolation_returnsClearMilestoneMessage() {
        request = new MockHttpServletRequest("POST", "/api/v1/admin/rewards");
        String pg = "ERROR: insert or update on table \"rewards\" violates foreign key constraint \"fk_rewards_milestone_id\"\n"
                + "Detail: Key (milestone_id)=(a52d3d70-37bc-473d-b68a-162694f39a1e) is not present in table \"milestones\".";
        var response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("could not execute statement", new RuntimeException(pg)),
                request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("FOREIGN_KEY_VIOLATION", response.getBody().code());
        assertTrue(response.getBody().message().contains("milestone_id"));
        assertTrue(response.getBody().message().contains("a52d3d70-37bc-473d-b68a-162694f39a1e"));
    }

    @Test
    void checkConstraint_returnsAllowedRewardTypes() {
        request = new MockHttpServletRequest("POST", "/api/v1/admin/rewards");
        String pg = "ERROR: new row for relation \"rewards\" violates check constraint \"chk_rewards_reward_type\"";
        var response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("could not execute statement", new RuntimeException(pg)),
                request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CHECK_CONSTRAINT_VIOLATION", response.getBody().code());
        assertTrue(response.getBody().message().contains("VOUCHER"));
    }

    @Test
    void notNullViolation_returnsClearColumnMessage() {
        request = new MockHttpServletRequest("POST", "/api/v1/admin/rewards");
        String pg = "ERROR: null value in column \"created_at\" of relation \"rewards\" violates not-null constraint";
        var response = handler.handleDataIntegrityViolation(
                new DataIntegrityViolationException("could not execute statement", new RuntimeException(pg)),
                request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("NOT_NULL_VIOLATION", response.getBody().code());
        assertTrue(response.getBody().message().contains("created_at"));
    }
}
