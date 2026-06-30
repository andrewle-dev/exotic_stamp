package metro.ExoticStamp.common.exceptions;

import metro.ExoticStamp.common.response.ErrorResponse;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidCredentialsException;
import metro.ExoticStamp.modules.auth.domain.exception.InvalidTokenException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpExpiredException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpInvalidException;
import metro.ExoticStamp.modules.auth.domain.exception.OtpMaxAttemptsExceededException;
import metro.ExoticStamp.modules.auth.domain.exception.ResendCooldownException;
import metro.ExoticStamp.modules.auth.domain.exception.SecurityBreachException;
import metro.ExoticStamp.modules.auth.domain.exception.TokenExpiredException;
import metro.ExoticStamp.modules.auth.domain.exception.AccountNotVerifiedException;
import metro.ExoticStamp.modules.auth.domain.exception.UserNotActiveException;
import metro.ExoticStamp.common.exceptions.storage.FileTooLargeException;
import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import metro.ExoticStamp.common.exceptions.storage.InvalidImageTypeException;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignArchivedException;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignCodeDuplicateException;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotActiveException;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignStationDuplicateException;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignStationNotEligibleException;
import metro.ExoticStamp.modules.collection.domain.exception.DefaultCampaignAmbiguousException;
import metro.ExoticStamp.modules.collection.domain.exception.DefaultCampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsAccuracyTooLowException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsInvalidException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsOutOfRangeException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsRequiredException;
import metro.ExoticStamp.modules.collection.domain.exception.StampDesignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.exception.DuplicateActiveStampDesignException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsVerificationFailedException;
import metro.ExoticStamp.modules.collection.domain.exception.IdempotencyKeyConflictException;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidRequestException;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidScanInputException;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidStationException;
import metro.ExoticStamp.modules.collection.domain.exception.StampAlreadyCollectedException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateLineCodeException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateNfcTagException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateQrTokenException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateStationCodeException;
import metro.ExoticStamp.modules.metro.domain.exception.DuplicateStationSequenceException;
import metro.ExoticStamp.modules.metro.domain.exception.InvalidScanPayloadException;
import metro.ExoticStamp.modules.metro.domain.exception.InvalidStationStatusException;
import metro.ExoticStamp.modules.metro.domain.exception.LineInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.LineNotFoundException;
import metro.ExoticStamp.modules.metro.domain.exception.ScanKeyInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.ScanKeyNotFoundException;
import metro.ExoticStamp.modules.metro.domain.exception.StationInactiveException;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.rbac.domain.exception.DuplicateRbacMappingException;
import metro.ExoticStamp.modules.rbac.domain.exception.ImmutableRoleException;
import metro.ExoticStamp.modules.rbac.domain.exception.LastAdminProtectionException;
import metro.ExoticStamp.modules.rbac.domain.exception.PermissionAlreadyExistsException;
import metro.ExoticStamp.modules.rbac.domain.exception.PermissionNotFoundException;
import metro.ExoticStamp.modules.rbac.domain.exception.RoleAlreadyAssignedException;
import metro.ExoticStamp.modules.rbac.domain.exception.RoleCodeAlreadyExistsException;
import metro.ExoticStamp.modules.rbac.domain.exception.RoleNotFoundException;
import metro.ExoticStamp.modules.user.domain.exception.UserFieldAlreadyTakenException;
import metro.ExoticStamp.modules.user.domain.exception.UserNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneAlreadyActiveException;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneAlreadyInactiveException;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.PartnerAlreadyActiveException;
import metro.ExoticStamp.modules.reward.domain.exception.PartnerAlreadyInactiveException;
import metro.ExoticStamp.modules.reward.domain.exception.PartnerNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.RewardAlreadyActiveException;
import metro.ExoticStamp.modules.reward.domain.exception.RewardAlreadyInactiveException;
import metro.ExoticStamp.modules.reward.domain.exception.RewardAlreadyIssuedException;
import metro.ExoticStamp.modules.reward.domain.exception.RewardNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.RedeemNotSupportedException;
import metro.ExoticStamp.modules.reward.domain.exception.RewardNotRedeemableException;
import metro.ExoticStamp.modules.reward.domain.exception.InvalidMilestoneStateException;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneArchivedException;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneCodeDuplicateException;
import metro.ExoticStamp.modules.reward.domain.exception.VoucherCodeDuplicateException;
import metro.ExoticStamp.modules.reward.domain.exception.VoucherCodeExhaustedException;
import metro.ExoticStamp.modules.community.domain.exception.NotificationNotFoundException;
import metro.ExoticStamp.modules.community.domain.exception.ReferralAlreadyAppliedException;
import metro.ExoticStamp.modules.community.domain.exception.ReferralCodeInactiveException;
import metro.ExoticStamp.modules.community.domain.exception.ReferralCodeNotFoundException;
import metro.ExoticStamp.modules.community.domain.exception.ReferralConflictException;
import metro.ExoticStamp.modules.community.domain.exception.SelfReferralNotAllowedException;
import metro.ExoticStamp.modules.community.domain.exception.SharePlatformInvalidException;
import metro.ExoticStamp.modules.community.domain.exception.ShareTypeInvalidException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.RollbackException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Pattern PG_DUP_KEY_PATTERN =
            Pattern.compile("Key \\((?<field>[^)]+)\\)=\\((?<value>[^)]+)\\) already exists\\.");

    @ExceptionHandler({UserNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "USER_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler({RoleNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(RoleNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "ROLE_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(PermissionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePermissionNotFound(PermissionNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "PERMISSION_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(LastAdminProtectionException.class)
    public ResponseEntity<ErrorResponse> handleLastAdmin(LastAdminProtectionException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "LAST_ADMIN_PROTECTION", ex.getMessage(), req);
    }

    @ExceptionHandler(ImmutableRoleException.class)
    public ResponseEntity<ErrorResponse> handleImmutableRole(ImmutableRoleException ex, HttpServletRequest req) {
        log.warn("[403] {}", ex.getMessage());
        return build(403, "IMMUTABLE_ROLE", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateRbacMappingException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateMapping(DuplicateRbacMappingException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "RBAC_DUPLICATE_MAPPING", ex.getMessage(), req);
    }

    @ExceptionHandler(PermissionAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handlePermissionExists(PermissionAlreadyExistsException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "PERMISSION_ALREADY_EXISTS", ex.getMessage(), req);
    }

    @ExceptionHandler(RoleCodeAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleRoleCodeExists(RoleCodeAlreadyExistsException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "ROLE_CODE_ALREADY_EXISTS", ex.getMessage(), req);
    }

    @ExceptionHandler({ObjectOptimisticLockingFailureException.class, OptimisticLockException.class})
    public ResponseEntity<ErrorResponse> handleOptimisticLock(RuntimeException ex, HttpServletRequest req) {
        log.warn("[409] Optimistic lock: {}", ex.getMessage());
        return build(409, "CONCURRENT_MODIFICATION", "Resource was modified by another transaction", req);
    }

    @ExceptionHandler({LineNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleLineNotFound(LineNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "LINE_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler({StationNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleStationNotFound(StationNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "STATION_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateLineCodeException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateLineCode(DuplicateLineCodeException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "LINE_CODE_DUPLICATE", ex.getMessage(), req);
    }

    @ExceptionHandler(ScanKeyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleScanKeyNotFound(ScanKeyNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "SCAN_KEY_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidScanPayloadException.class)
    public ResponseEntity<ErrorResponse> handleInvalidScanPayload(InvalidScanPayloadException ex, HttpServletRequest req) {
        log.warn("[400] {}", ex.getMessage());
        return build(400, "SCAN_PAYLOAD_INVALID", ex.getMessage(), req);
    }

    @ExceptionHandler(StationInactiveException.class)
    public ResponseEntity<ErrorResponse> handleStationInactive(StationInactiveException ex, HttpServletRequest req) {
        log.warn("[422] {}", ex.getMessage());
        return build(422, "STATION_INACTIVE", ex.getMessage(), req);
    }

    @ExceptionHandler(LineInactiveException.class)
    public ResponseEntity<ErrorResponse> handleLineInactive(LineInactiveException ex, HttpServletRequest req) {
        log.warn("[422] {}", ex.getMessage());
        return build(422, "LINE_INACTIVE", ex.getMessage(), req);
    }

    @ExceptionHandler(ScanKeyInactiveException.class)
    public ResponseEntity<ErrorResponse> handleScanKeyInactive(ScanKeyInactiveException ex, HttpServletRequest req) {
        log.warn("[422] {}", ex.getMessage());
        return build(422, "SCAN_KEY_INACTIVE", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidStationStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStationStatus(InvalidStationStatusException ex, HttpServletRequest req) {
        log.warn("[422] {}", ex.getMessage());
        return build(422, "INVALID_STATION_STATUS", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateNfcTagException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateNfc(DuplicateNfcTagException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "NFC_TAG_DUPLICATE", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateQrTokenException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateQr(DuplicateQrTokenException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "QR_TOKEN_DUPLICATE", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateStationCodeException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateStationCode(DuplicateStationCodeException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "STATION_CODE_DUPLICATE", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateStationSequenceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateStationSequence(DuplicateStationSequenceException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "STATION_SEQUENCE_DUPLICATE", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidImageTypeException.class)
    public ResponseEntity<ErrorResponse> handleInvalidImageType(InvalidImageTypeException ex, HttpServletRequest req) {
        log.warn("[400] {}", ex.getMessage());
        return build(400, "INVALID_IMAGE_TYPE", ex.getMessage(), req);
    }

    @ExceptionHandler(FileTooLargeException.class)
    public ResponseEntity<ErrorResponse> handleFileTooLarge(FileTooLargeException ex, HttpServletRequest req) {
        log.warn("[400] {}", ex.getMessage());
        return build(400, "FILE_TOO_LARGE", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidFileException.class)
    public ResponseEntity<ErrorResponse> handleInvalidFile(InvalidFileException ex, HttpServletRequest req) {
        log.warn("[400] {}", ex.getMessage());
        return build(400, "INVALID_FILE", ex.getMessage(), req);
    }

    @ExceptionHandler(UserFieldAlreadyTakenException.class)
    public ResponseEntity<ErrorResponse> handleEmailTaken(UserFieldAlreadyTakenException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "USER_TAKEN", ex.getMessage(), req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException ex,
            HttpServletRequest req
    ) {
        Throwable root = unwrap(ex);
        String rootMsg = root.getMessage() == null ? "" : root.getMessage();

        // Postgres duplicate key (unique constraint) -> return 409 with a clear field message
        Matcher m = PG_DUP_KEY_PATTERN.matcher(rootMsg);
        if (m.find()) {
            String field = m.group("field");
            String value = m.group("value");

            String code;
            String msg;
            if ("phone_number".equals(field)) {
                code = "PHONE_NUMBER_TAKEN";
                msg = "Phone number already taken";
            } else if ("email".equals(field)) {
                code = "EMAIL_TAKEN";
                msg = "Email already taken";
            } else if ("username".equals(field)) {
                code = "USERNAME_TAKEN";
                msg = "Username already taken";
            } else {
                code = "DUPLICATE_FIELD";
                msg = "A value for this field is already in use";
            }

            log.warn("[409] DataIntegrity duplicate {}={}", field, value);
            return build(409, code, msg, req);
        }

        // Fallback: still a conflict, but don't leak internal constraint details
        log.warn("[409] DataIntegrityViolation at {} {}: {}", req.getMethod(), req.getRequestURI(), rootMsg);
        return build(409, "DATA_INTEGRITY_VIOLATION", "Duplicate or conflicting data", req);
    }

    @ExceptionHandler(RoleAlreadyAssignedException.class)
    public ResponseEntity<ErrorResponse> handleEmailTaken(RoleAlreadyAssignedException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "ROLE_ALREADY_ASSIGNED", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ErrorResponse> handleInvalidRequest(InvalidRequestException ex, HttpServletRequest req) {
        log.warn("[400] InvalidRequest: {}", ex.getMessage());
        return build(400, "INVALID_REQUEST", ex.getMessage(), req);
    }

    @ExceptionHandler(IdempotencyKeyConflictException.class)
    public ResponseEntity<ErrorResponse> handleIdempotencyConflict(IdempotencyKeyConflictException ex, HttpServletRequest req) {
        log.warn("[409] IdempotencyKeyConflict: {}", ex.getMessage());
        return build(409, "IDEMPOTENCY_KEY_CONFLICT", ex.getMessage(), req);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleBadArg(IllegalArgumentException ex, HttpServletRequest req) {
        log.warn("[400] IllegalArgument: {}", ex.getMessage());
        return build(400, "VALIDATION_ERROR", ex.getMessage(), req);
    }

    @ExceptionHandler(StampAlreadyCollectedException.class)
    public ResponseEntity<ErrorResponse> handleStampAlreadyCollected(StampAlreadyCollectedException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "STAMP_ALREADY_COLLECTED", ex.getMessage(), req);
    }

    @ExceptionHandler(CampaignNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleCampaignNotFound(CampaignNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "CAMPAIGN_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(CampaignArchivedException.class)
    public ResponseEntity<ErrorResponse> handleCampaignArchived(CampaignArchivedException ex, HttpServletRequest req) {
        log.warn("[422] {}", ex.getMessage());
        return build(422, "CAMPAIGN_ARCHIVED", ex.getMessage(), req);
    }

    @ExceptionHandler(CampaignCodeDuplicateException.class)
    public ResponseEntity<ErrorResponse> handleCampaignCodeDuplicate(CampaignCodeDuplicateException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "CAMPAIGN_CODE_DUPLICATE", ex.getMessage(), req);
    }

    @ExceptionHandler(CampaignStationDuplicateException.class)
    public ResponseEntity<ErrorResponse> handleCampaignStationDuplicate(CampaignStationDuplicateException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "CAMPAIGN_STATION_DUPLICATE", ex.getMessage(), req);
    }

    @ExceptionHandler(DuplicateActiveStampDesignException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateActiveStampDesign(DuplicateActiveStampDesignException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "STAMP_DESIGN_ACTIVE_DUPLICATE", ex.getMessage(), req);
    }

    @ExceptionHandler(CampaignNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleCampaignNotActive(CampaignNotActiveException ex, HttpServletRequest req) {
        log.warn("[422] {}", ex.getMessage());
        return build(422, "CAMPAIGN_NOT_ACTIVE", ex.getMessage(), req);
    }

    @ExceptionHandler(DefaultCampaignNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDefaultCampaignNotFound(DefaultCampaignNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "DEFAULT_CAMPAIGN_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(DefaultCampaignAmbiguousException.class)
    public ResponseEntity<ErrorResponse> handleDefaultCampaignAmbiguous(DefaultCampaignAmbiguousException ex, HttpServletRequest req) {
        log.warn("[422] {}", ex.getMessage());
        return build(422, "DEFAULT_CAMPAIGN_AMBIGUOUS", ex.getMessage(), req);
    }

    @ExceptionHandler(CampaignStationNotEligibleException.class)
    public ResponseEntity<ErrorResponse> handleCampaignStationNotEligible(CampaignStationNotEligibleException ex, HttpServletRequest req) {
        log.warn("[422] {}", ex.getMessage());
        return build(422, "CAMPAIGN_STATION_NOT_ELIGIBLE", ex.getMessage(), req);
    }

    @ExceptionHandler(StampDesignNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleStampDesignNotFound(StampDesignNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "STAMP_DESIGN_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(GpsRequiredException.class)
    public ResponseEntity<ErrorResponse> handleGpsRequired(GpsRequiredException ex, HttpServletRequest req) {
        log.warn("[400] {}", ex.getMessage());
        return build(400, "GPS_REQUIRED", ex.getMessage(), req);
    }

    @ExceptionHandler(GpsInvalidException.class)
    public ResponseEntity<ErrorResponse> handleGpsInvalid(GpsInvalidException ex, HttpServletRequest req) {
        log.warn("[400] {}", ex.getMessage());
        return build(400, "GPS_INVALID", ex.getMessage(), req);
    }

    @ExceptionHandler(GpsAccuracyTooLowException.class)
    public ResponseEntity<ErrorResponse> handleGpsAccuracyTooLow(GpsAccuracyTooLowException ex, HttpServletRequest req) {
        log.warn("[422] {}", ex.getMessage());
        return build(422, "GPS_ACCURACY_TOO_LOW", ex.getMessage(), req);
    }

    @ExceptionHandler(GpsOutOfRangeException.class)
    public ResponseEntity<ErrorResponse> handleGpsOutOfRange(GpsOutOfRangeException ex, HttpServletRequest req) {
        log.warn("[422] {}", ex.getMessage());
        return build(422, "GPS_OUT_OF_RANGE", ex.getMessage(), req);
    }

    @ExceptionHandler(GpsVerificationFailedException.class)
    public ResponseEntity<ErrorResponse> handleGpsVerificationFailed(GpsVerificationFailedException ex, HttpServletRequest req) {
        log.warn("[400] {}", ex.getMessage());
        return build(400, "GPS_VERIFICATION_FAILED", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidScanInputException.class)
    public ResponseEntity<ErrorResponse> handleInvalidScanInput(InvalidScanInputException ex, HttpServletRequest req) {
        log.warn("[400] {}", ex.getMessage());
        return build(400, "INVALID_SCAN_METHOD", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidStationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidStation(InvalidStationException ex, HttpServletRequest req) {
        log.warn("[400] {}", ex.getMessage());
        return build(400, "INVALID_STATION", ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBeanValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("[400] BeanValidation: {}", msg);
        return build(400, "INVALID_INPUT", msg, req);
    }

    @ExceptionHandler(RollbackException.class)
    public ResponseEntity<ErrorResponse> handleRollback(RollbackException ex, HttpServletRequest req) {
        Throwable root = unwrap(ex);
        log.warn("[400] RollbackException: {}", root.getMessage());
        return build(400, "ENTITY_VALIDATION_ERROR", "Request could not be processed due to a validation error", req);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        String msg = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", "));
        log.warn("[400] ConstraintViolation: {}", msg);
        return build(400, "CONSTRAINT_VIOLATION", msg, req);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("[403] AccessDenied: {} {}", req.getMethod(), req.getRequestURI());
        return build(403, "ACCESS_DENIED", "You don't have permission to access this resource", req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuth(AuthenticationException ex, HttpServletRequest req) {
        log.warn("[401] Unauthenticated: {}", ex.getMessage());
        return build(401, "UNAUTHORIZED", "Authentication required", req);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(Exception ex, HttpServletRequest req) {
        log.error("[500] Unhandled at {} {}: ", req.getMethod(), req.getRequestURI(), ex);
        return build(500, "INTERNAL_ERROR", "An unexpected error occurred", req);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(
            InvalidCredentialsException ex,
            HttpServletRequest req
    ) {
        log.warn("[401] Invalid credentials: {}", ex.getMessage());
        return build(401, "INVALID_CREDENTIALS", ex.getMessage(), req);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<ErrorResponse> handleTokenExpired(
            TokenExpiredException ex,
            HttpServletRequest req
    ) {
        log.warn("[401] Token expired: {}", ex.getMessage());
        return build(401, "TOKEN_EXPIRED", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ErrorResponse> handleInvalidToken(
            InvalidTokenException ex,
            HttpServletRequest req
    ) {
        log.warn("[401] Invalid token: {}", ex.getMessage());
        return build(401, "INVALID_TOKEN", ex.getMessage(), req);
    }

    @ExceptionHandler(OtpExpiredException.class)
    public ResponseEntity<ErrorResponse> handleOtpExpired(
            OtpExpiredException ex,
            HttpServletRequest req
    ) {
        log.warn("[400] OTP expired: {}", ex.getMessage());
        return build(400, "OTP_EXPIRED", ex.getMessage(), req);
    }

    @ExceptionHandler(OtpInvalidException.class)
    public ResponseEntity<ErrorResponse> handleOtpInvalid(
            OtpInvalidException ex,
            HttpServletRequest req
    ) {
        log.warn("[400] OTP invalid: {}", ex.getMessage());
        return build(400, "OTP_INVALID", ex.getMessage(), req);
    }

    @ExceptionHandler(AccountNotVerifiedException.class)
    public ResponseEntity<ErrorResponse> handleAccountNotVerified(
            AccountNotVerifiedException ex,
            HttpServletRequest req
    ) {
        log.warn("[403] Account not verified: {}", ex.getMessage());
        return build(403, "ACCOUNT_NOT_VERIFIED", ex.getMessage(), req);
    }

    @ExceptionHandler(UserNotActiveException.class)
    public ResponseEntity<ErrorResponse> handleUserNotActive(
            UserNotActiveException ex,
            HttpServletRequest req
    ) {
        log.warn("[403] User not active: {}", ex.getMessage());
        return build(403, "USER_NOT_ACTIVE", ex.getMessage(), req);
    }

    @ExceptionHandler(SecurityBreachException.class)
    public ResponseEntity<ErrorResponse> handleSecurityBreach(
            SecurityBreachException ex,
            HttpServletRequest req
    ) {
        log.error("[401] Security breach: {}", ex.getMessage());
        return build(401, "SECURITY_BREACH", ex.getMessage(), req);
    }

    @ExceptionHandler(ResendCooldownException.class)
    public ResponseEntity<ErrorResponse> handleResendCooldown(
            ResendCooldownException ex,
            HttpServletRequest req
    ) {
        log.warn("[429] ResendCooldown: {} seconds remaining", ex.getSecondsRemaining());
        return build(429, "RESEND_COOLDOWN", ex.getMessage(), req);
    }

    @ExceptionHandler(OtpMaxAttemptsExceededException.class)
    public ResponseEntity<ErrorResponse> handleOtpMaxAttempts(
            OtpMaxAttemptsExceededException ex, HttpServletRequest req) {
        log.warn("[429] OtpMaxAttempts: max={} at {}", ex.getMaxAttempts(), req.getRequestURI());
        return ResponseEntity.status(429)
                .body(ErrorResponse.of(
                        "OTP_MAX_ATTEMPTS_EXCEEDED",
                        ex.getMessage(),
                        429,
                        req.getRequestURI()));
    }

    private ResponseEntity<ErrorResponse> build(int status, String code, String message, HttpServletRequest req) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(code, message, status, req.getRequestURI()));
    }

    @ExceptionHandler(PartnerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePartnerNotFound(PartnerNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "PARTNER_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(MilestoneNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMilestoneNotFound(MilestoneNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "MILESTONE_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(MilestoneCodeDuplicateException.class)
    public ResponseEntity<ErrorResponse> handleMilestoneCodeDuplicate(MilestoneCodeDuplicateException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "MILESTONE_CODE_DUPLICATE", ex.getMessage(), req);
    }

    @ExceptionHandler(MilestoneArchivedException.class)
    public ResponseEntity<ErrorResponse> handleMilestoneArchived(MilestoneArchivedException ex, HttpServletRequest req) {
        log.warn("[422] {}", ex.getMessage());
        return build(422, "MILESTONE_ARCHIVED", ex.getMessage(), req);
    }

    @ExceptionHandler(VoucherCodeDuplicateException.class)
    public ResponseEntity<ErrorResponse> handleVoucherCodeDuplicate(VoucherCodeDuplicateException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "VOUCHER_CODE_DUPLICATE", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidMilestoneStateException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMilestoneState(InvalidMilestoneStateException ex, HttpServletRequest req) {
        log.warn("[400] {}", ex.getMessage());
        return build(400, "INVALID_MILESTONE_STATE", ex.getMessage(), req);
    }

    @ExceptionHandler(RewardNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleRewardNotFound(RewardNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "REWARD_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(RedeemNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleRedeemNotSupported(RedeemNotSupportedException ex, HttpServletRequest req) {
        log.warn("[410] {}", ex.getMessage());
        return build(410, "REDEEM_NOT_SUPPORTED", ex.getMessage(), req);
    }

    @ExceptionHandler(RewardNotRedeemableException.class)
    public ResponseEntity<ErrorResponse> handleRewardNotRedeemable(RewardNotRedeemableException ex, HttpServletRequest req) {
        log.warn("[422] {}", ex.getMessage());
        return build(422, "REWARD_NOT_REDEEMABLE", ex.getMessage(), req);
    }

    @ExceptionHandler(VoucherCodeExhaustedException.class)
    public ResponseEntity<ErrorResponse> handleVoucherExhausted(VoucherCodeExhaustedException ex, HttpServletRequest req) {
        log.warn("[422] {}", ex.getMessage());
        return build(422, "VOUCHER_EXHAUSTED", ex.getMessage(), req);
    }

    @ExceptionHandler(RewardAlreadyIssuedException.class)
    public ResponseEntity<ErrorResponse> handleRewardAlreadyIssued(RewardAlreadyIssuedException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "REWARD_ALREADY_ISSUED", ex.getMessage(), req);
    }

    @ExceptionHandler(MilestoneAlreadyActiveException.class)
    public ResponseEntity<ErrorResponse> handleMilestoneAlreadyActive(MilestoneAlreadyActiveException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "MILESTONE_ALREADY_ACTIVE", ex.getMessage(), req);
    }

    @ExceptionHandler(MilestoneAlreadyInactiveException.class)
    public ResponseEntity<ErrorResponse> handleMilestoneAlreadyInactive(MilestoneAlreadyInactiveException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "MILESTONE_ALREADY_INACTIVE", ex.getMessage(), req);
    }

    @ExceptionHandler(PartnerAlreadyActiveException.class)
    public ResponseEntity<ErrorResponse> handlePartnerAlreadyActive(PartnerAlreadyActiveException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "PARTNER_ALREADY_ACTIVE", ex.getMessage(), req);
    }

    @ExceptionHandler(PartnerAlreadyInactiveException.class)
    public ResponseEntity<ErrorResponse> handlePartnerAlreadyInactive(PartnerAlreadyInactiveException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "PARTNER_ALREADY_INACTIVE", ex.getMessage(), req);
    }

    @ExceptionHandler(RewardAlreadyActiveException.class)
    public ResponseEntity<ErrorResponse> handleRewardAlreadyActive(RewardAlreadyActiveException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "REWARD_ALREADY_ACTIVE", ex.getMessage(), req);
    }

    @ExceptionHandler(RewardAlreadyInactiveException.class)
    public ResponseEntity<ErrorResponse> handleRewardAlreadyInactive(RewardAlreadyInactiveException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "REWARD_ALREADY_INACTIVE", ex.getMessage(), req);
    }

    @ExceptionHandler(ReferralCodeNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleReferralCodeNotFound(ReferralCodeNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "REFERRAL_CODE_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(ReferralCodeInactiveException.class)
    public ResponseEntity<ErrorResponse> handleReferralCodeInactive(ReferralCodeInactiveException ex, HttpServletRequest req) {
        log.warn("[422] {}", ex.getMessage());
        return build(422, "REFERRAL_CODE_INACTIVE", ex.getMessage(), req);
    }

    @ExceptionHandler(SelfReferralNotAllowedException.class)
    public ResponseEntity<ErrorResponse> handleSelfReferral(SelfReferralNotAllowedException ex, HttpServletRequest req) {
        log.warn("[422] {}", ex.getMessage());
        return build(422, "SELF_REFERRAL_NOT_ALLOWED", ex.getMessage(), req);
    }

    @ExceptionHandler(ReferralAlreadyAppliedException.class)
    public ResponseEntity<ErrorResponse> handleReferralAlreadyApplied(ReferralAlreadyAppliedException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "REFERRAL_ALREADY_APPLIED", ex.getMessage(), req);
    }

    @ExceptionHandler(ReferralConflictException.class)
    public ResponseEntity<ErrorResponse> handleReferralConflict(ReferralConflictException ex, HttpServletRequest req) {
        log.warn("[409] {}", ex.getMessage());
        return build(409, "REFERRAL_CONFLICT", ex.getMessage(), req);
    }

    @ExceptionHandler(SharePlatformInvalidException.class)
    public ResponseEntity<ErrorResponse> handleSharePlatformInvalid(SharePlatformInvalidException ex, HttpServletRequest req) {
        log.warn("[400] {}", ex.getMessage());
        return build(400, "SHARE_PLATFORM_INVALID", ex.getMessage(), req);
    }

    @ExceptionHandler(ShareTypeInvalidException.class)
    public ResponseEntity<ErrorResponse> handleShareTypeInvalid(ShareTypeInvalidException ex, HttpServletRequest req) {
        log.warn("[400] {}", ex.getMessage());
        return build(400, "SHARE_TYPE_INVALID", ex.getMessage(), req);
    }

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotificationNotFound(NotificationNotFoundException ex, HttpServletRequest req) {
        log.warn("[404] {}", ex.getMessage());
        return build(404, "NOTIFICATION_NOT_FOUND", ex.getMessage(), req);
    }

    // 422 — business rule violation
    // Khác với 400 (bad input format) và 409 (conflict/duplicate)
    @ExceptionHandler(DomainRuleViolationException.class)
    public ResponseEntity<ErrorResponse> handleDomainRule(
            DomainRuleViolationException ex, HttpServletRequest req) {
        log.warn("[422] DomainRule: {}", ex.getMessage());
        return build(422, "DOMAIN_RULE_VIOLATION", ex.getMessage(), req);
    }

    private Throwable unwrap(Throwable t) {
        return t.getCause() != null ? unwrap(t.getCause()) : t;
    }
}
