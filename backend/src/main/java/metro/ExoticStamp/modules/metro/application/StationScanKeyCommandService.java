package metro.ExoticStamp.modules.metro.application;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.metro.application.command.ActivateStationScanKeyCommand;
import metro.ExoticStamp.modules.metro.application.command.CreateStationScanKeyCommand;
import metro.ExoticStamp.modules.metro.application.command.RevokeStationScanKeyCommand;
import metro.ExoticStamp.modules.metro.application.command.VerifyStationScanKeyInstallationCommand;
import metro.ExoticStamp.modules.metro.application.mapper.StationScanKeyAppMapper;
import metro.ExoticStamp.modules.metro.application.support.MetroEnumParser;
import metro.ExoticStamp.modules.metro.application.support.ScanKeyHasher;
import metro.ExoticStamp.modules.metro.application.support.ScanKeyRedactor;
import metro.ExoticStamp.modules.metro.application.support.ScanPayloadParser;
import metro.ExoticStamp.modules.metro.application.view.StationScanKeyCreatedView;
import metro.ExoticStamp.modules.metro.application.view.StationScanKeyVerifyView;
import metro.ExoticStamp.modules.metro.application.view.StationScanKeyView;
import metro.ExoticStamp.modules.metro.domain.MetroAuditConstants;
import metro.ExoticStamp.modules.metro.domain.exception.InvalidScanPayloadException;
import metro.ExoticStamp.modules.metro.domain.exception.ScanKeyAlreadyActiveException;
import metro.ExoticStamp.modules.metro.domain.exception.ScanKeyNotFoundException;
import metro.ExoticStamp.modules.metro.domain.exception.StationNotFoundException;
import metro.ExoticStamp.modules.metro.domain.model.ScanKeyStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanType;
import metro.ExoticStamp.modules.metro.domain.model.StationScanKey;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationScanKeyRepository;
import metro.ExoticStamp.modules.metro.application.support.MetroAuditHelper;
import metro.ExoticStamp.modules.rbac.application.support.RbacSecurityContextHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StationScanKeyCommandService {

    private static final int KEY_PREFIX_LENGTH = 12;
    private static final int RAW_KEY_BYTES = 24;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final StationScanKeyRepository stationScanKeyRepository;
    private final StationRepository stationRepository;
    private final ScanKeyHasher scanKeyHasher;
    private final ScanPayloadParser scanPayloadParser;
    private final StationScanKeyAppMapper mapper;
    private final RbacSecurityContextHelper securityContextHelper;
    private final MetroAuditHelper metroAuditHelper;
    private final Clock clock;

    @Transactional
    public StationScanKeyCreatedView create(CreateStationScanKeyCommand command) {
        UUID stationId = command.getStationId();
        if (!stationRepository.findById(stationId).isPresent()) {
            throw new StationNotFoundException(stationId);
        }

        ScanType scanType = MetroEnumParser.parseScanType(command.getScanType());
        if (scanType == null) {
            throw new InvalidScanPayloadException("scanType is required");
        }

        String rawKey = generateRawKey(scanType);
        String keyHash = scanKeyHasher.hash(rawKey);
        String keyPrefix = rawKey.length() <= KEY_PREFIX_LENGTH
                ? rawKey
                : rawKey.substring(0, KEY_PREFIX_LENGTH);
        String payloadToWrite = scanPayloadParser.buildPayloadToWrite(rawKey);
        UUID actorId = securityContextHelper.currentUserId().orElse(null);
        LocalDateTime now = LocalDateTime.now(clock);

        StationScanKey key = StationScanKey.builder()
                .stationId(stationId)
                .scanType(scanType)
                .keyHash(keyHash)
                .keyPrefix(keyPrefix)
                .payloadScheme(StationScanKey.DEFAULT_PAYLOAD_SCHEME)
                .label(blankToNull(command.getLabel()))
                .placementNote(blankToNull(command.getPlacementNote()))
                .status(ScanKeyStatus.DRAFT)
                .createdBy(actorId)
                .createdAt(now)
                .updatedAt(now)
                .version(0L)
                .build();

        StationScanKey saved = stationScanKeyRepository.save(key);
        metroAuditHelper.schedule(
                MetroAuditConstants.TABLE_STATION_SCAN_KEYS,
                MetroAuditConstants.SCAN_KEY_CREATED,
                null,
                saved.getId() + ":" + ScanKeyRedactor.redact(rawKey));
        return mapper.toCreatedView(saved, payloadToWrite);
    }

    @Transactional
    public StationScanKeyView activate(ActivateStationScanKeyCommand command) {
        StationScanKey key = requireKey(command.getId());
        if (key.getStatus() == ScanKeyStatus.ACTIVE) {
            throw new ScanKeyAlreadyActiveException(key.getId());
        }
        if (!key.canActivate()) {
            throw new metro.ExoticStamp.modules.metro.domain.exception.ScanKeyInactiveException(
                    "Scan key cannot be activated from status " + key.getStatus());
        }
        LocalDateTime now = LocalDateTime.now(clock);
        key.setStatus(ScanKeyStatus.ACTIVE);
        key.setActivatedAt(now);
        key.setRevokedAt(null);
        key.setUpdatedAt(now);
        StationScanKey saved = stationScanKeyRepository.save(key);
        metroAuditHelper.schedule(
                MetroAuditConstants.TABLE_STATION_SCAN_KEYS,
                MetroAuditConstants.SCAN_KEY_ACTIVATED,
                null,
                saved.getId().toString());
        return mapper.toView(saved);
    }

    @Transactional
    public StationScanKeyView revoke(RevokeStationScanKeyCommand command) {
        StationScanKey key = requireKey(command.getId());
        assertNotTerminal(key);
        LocalDateTime now = LocalDateTime.now(clock);
        key.setStatus(ScanKeyStatus.REVOKED);
        key.setRevokedAt(now);
        key.setUpdatedAt(now);
        StationScanKey saved = stationScanKeyRepository.save(key);
        String reason = blankToNull(command.getReason());
        metroAuditHelper.schedule(
                MetroAuditConstants.TABLE_STATION_SCAN_KEYS,
                MetroAuditConstants.SCAN_KEY_REVOKED,
                null,
                saved.getId() + (reason != null ? ":" + reason : ""));
        return mapper.toView(saved);
    }

    @Transactional
    public StationScanKeyView markLost(UUID id) {
        StationScanKey key = requireKey(id);
        assertNotTerminal(key);
        LocalDateTime now = LocalDateTime.now(clock);
        key.setStatus(ScanKeyStatus.LOST);
        key.setRevokedAt(now);
        key.setUpdatedAt(now);
        StationScanKey saved = stationScanKeyRepository.save(key);
        metroAuditHelper.schedule(
                MetroAuditConstants.TABLE_STATION_SCAN_KEYS,
                MetroAuditConstants.SCAN_KEY_MARKED_LOST,
                null,
                saved.getId().toString());
        return mapper.toView(saved);
    }

    @Transactional
    public StationScanKeyVerifyView verifyInstallation(VerifyStationScanKeyInstallationCommand command) {
        StationScanKey key = requireKey(command.getId());
        String rawKey = scanPayloadParser.extractRawKey(command.getPayloadReadBack());
        String hash = scanKeyHasher.hash(rawKey);
        if (!hash.equals(key.getKeyHash())) {
            throw new InvalidScanPayloadException("payloadReadBack does not match this scan key");
        }

        LocalDateTime now = LocalDateTime.now(clock);
        UUID actorId = securityContextHelper.currentUserId().orElse(null);
        key.setLastInstallVerifiedAt(now);
        key.setInstalledLatitude(command.getLatitude());
        key.setInstalledLongitude(command.getLongitude());
        key.setInstalledAccuracyMeters(command.getAccuracyMeters());
        key.setInstalledDevicePlatform(blankToNull(command.getDevicePlatform()));
        key.setInstalledAppVersion(blankToNull(command.getAppVersion()));
        key.setInstalledBy(actorId);
        key.setUpdatedAt(now);
        StationScanKey saved = stationScanKeyRepository.save(key);

        metroAuditHelper.schedule(
                MetroAuditConstants.TABLE_STATION_SCAN_KEYS,
                MetroAuditConstants.SCAN_KEY_INSTALL_VERIFIED,
                null,
                saved.getId() + ":" + ScanKeyRedactor.redact(command.getPayloadReadBack()));
        return mapper.toVerifyView(saved);
    }

    /**
     * Updates last_seen_at for an ACTIVE key after a successful resolve/collect.
     * REQUIRES_NEW so the write succeeds even when called from a read-only resolve transaction.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordLastSeen(UUID id) {
        stationScanKeyRepository.updateLastSeenAt(id, LocalDateTime.now(clock));
    }

    private StationScanKey requireKey(UUID id) {
        return stationScanKeyRepository.findById(id).orElseThrow(ScanKeyNotFoundException::new);
    }

    private static void assertNotTerminal(StationScanKey key) {
        if (key.getStatus() == ScanKeyStatus.REVOKED
                || key.getStatus() == ScanKeyStatus.LOST
                || key.getStatus() == ScanKeyStatus.REPLACED) {
            throw new metro.ExoticStamp.modules.metro.domain.exception.ScanKeyInactiveException(
                    "Scan key is already " + key.getStatus());
        }
    }

    private static String generateRawKey(ScanType scanType) {
        byte[] bytes = new byte[RAW_KEY_BYTES];
        SECURE_RANDOM.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        String prefix = switch (scanType) {
            case NFC -> "nfc_";
            case QR_STATIC, QR_DYNAMIC_PLACEHOLDER -> "qr_";
        };
        return prefix + token;
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
