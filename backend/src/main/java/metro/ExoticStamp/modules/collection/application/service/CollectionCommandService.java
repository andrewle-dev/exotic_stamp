package metro.ExoticStamp.modules.collection.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.collection.application.command.CollectStampCommand;
import metro.ExoticStamp.modules.collection.application.port.StationScanResolverPort;
import metro.ExoticStamp.modules.collection.application.port.UserStampCachePort;
import metro.ExoticStamp.modules.collection.application.support.CollectionEnumParser;
import metro.ExoticStamp.modules.collection.application.support.CollectionPolicyService;
import metro.ExoticStamp.modules.collection.application.support.CollectionRuntimeAuditHelper;
import metro.ExoticStamp.modules.collection.application.support.DefaultCampaignResolver;
import metro.ExoticStamp.modules.collection.application.support.GpsValidationService;
import metro.ExoticStamp.modules.collection.application.support.StampDesignResolver;
import metro.ExoticStamp.modules.collection.application.view.CollectStampResultView;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;
import metro.ExoticStamp.modules.collection.application.view.ResolvedStationView;
import metro.ExoticStamp.modules.collection.application.view.StampCollectView;
import metro.ExoticStamp.modules.collection.domain.event.StampCollectedEvent;
import metro.ExoticStamp.modules.collection.domain.exception.GpsAccuracyTooLowException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsInvalidException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsOutOfRangeException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsRequiredException;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidRequestException;
import metro.ExoticStamp.modules.collection.domain.exception.StampAlreadyCollectedException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;
import metro.ExoticStamp.modules.collection.domain.model.CollectionPolicy;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.policy.CollectionEligibilityPolicy;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import metro.ExoticStamp.modules.metro.application.port.LineReadPort;
import metro.ExoticStamp.modules.metro.application.port.StationReadPort;
import metro.ExoticStamp.modules.metro.application.view.MetroStationView;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionCommandService {

    private final StationScanResolverPort stationScanResolverPort;
    private final DefaultCampaignResolver defaultCampaignResolver;
    private final CampaignStationRepository campaignStationRepository;
    private final StampDesignResolver stampDesignResolver;
    private final GpsValidationService gpsValidationService;
    private final CollectionPolicyService collectionPolicyService;
    private final UserStampRepository userStampRepository;
    private final StampDesignRepository stampDesignRepository;
    private final CollectionQueryService collectionQueryService;
    private final UserStampCachePort cachePort;
    private final ApplicationEventPublisher eventPublisher;
    private final CollectionRuntimeAuditHelper auditHelper;
    private final StationReadPort stationReadPort;
    private final LineReadPort lineReadPort;
    private final Clock clock;
    private final MeterRegistry meterRegistry;

    @Transactional
    public CollectStampResultView collect(CollectStampCommand cmd) {
        validateCommand(cmd);

        String scanType = CollectionEnumParser.parseScanType(cmd.scanType());
        if (cmd.payload() == null || cmd.payload().isBlank()) {
            throw new InvalidRequestException("payload is required");
        }

        UUID idempotencyKey = cmd.idempotencyKey() != null ? cmd.idempotencyKey() : UUID.randomUUID();
        String idempotencyKeyStr = idempotencyKey.toString();

        Optional<UserStamp> replay = collectionPolicyService.resolveIdempotentReplay(idempotencyKeyStr, cmd.userId());
        if (replay.isPresent()) {
            return buildResult(replay.get(), false);
        }

        ResolvedStationView station = stationScanResolverPort.resolve(scanType, cmd.payload().trim());
        Campaign campaign = defaultCampaignResolver.resolveActiveGlobalDefault(station.lineId());

        CollectionEligibilityPolicy.assertCampaignStationEligible(
                campaignStationRepository.exists(campaign.getId(), station.id()),
                campaign.getId(),
                station.id());

        StampDesign design = stampDesignResolver.resolveActive(campaign.getId(), station.id());

        GpsValidationService.GpsValidationResult gpsResult;
        try {
            gpsResult = gpsValidationService.validate(
                    cmd.latitude(), cmd.longitude(), cmd.accuracyMeters(), station);
        } catch (GpsRequiredException ex) {
            auditHelper.scheduleGpsValidationFailed("GPS_REQUIRED", null);
            throw ex;
        } catch (GpsInvalidException ex) {
            auditHelper.scheduleGpsValidationFailed("GPS_INVALID", null);
            throw ex;
        } catch (GpsAccuracyTooLowException ex) {
            auditHelper.scheduleGpsValidationFailed("GPS_ACCURACY_TOO_LOW", null);
            throw ex;
        } catch (GpsOutOfRangeException ex) {
            auditHelper.scheduleGpsValidationFailed("GPS_OUT_OF_RANGE", null);
            throw ex;
        }

        collectionPolicyService.assertCollectAllowed(cmd.userId(), station.id(), campaign.getId());

        CollectMethod collectMethod = toCollectMethod(scanType);
        LocalDateTime now = LocalDateTime.now(clock);

        UserStamp toSave = UserStamp.builder()
                .userId(cmd.userId())
                .stationId(station.id())
                .campaignId(campaign.getId())
                .stampDesignId(design.getId())
                .collectedAt(now)
                .latitude(cmd.latitude())
                .longitude(cmd.longitude())
                .gpsVerified(gpsResult.verified())
                .gpsDistanceMeters(gpsResult.distanceMeters())
                .gpsAccuracyMeters(gpsResult.accuracyMeters())
                .collectMethod(collectMethod)
                .sourceScanType(scanType)
                .deviceFingerprint(resolveDeviceFingerprint(cmd))
                .devicePlatform(cmd.devicePlatform())
                .appVersion(cmd.appVersion())
                .collectionPolicy(CollectionPolicy.MVP_ONCE_PER_STATION_CAMPAIGN)
                .idempotencyKey(idempotencyKeyStr)
                .createdAt(now)
                .build();

        UserStamp saved;
        try {
            saved = userStampRepository.save(toSave);
        } catch (DataIntegrityViolationException ex) {
            if (isUserStampCollectUniqueViolation(ex)) {
                throw new StampAlreadyCollectedException(station.id());
            }
            throw ex;
        }

        cachePort.evictAllForUserCollection(cmd.userId(), station.lineId(), campaign.getId());

        // Publish inside the transaction so @TransactionalEventListener(AFTER_COMMIT) listeners
        // run only after successful commit. Payload is immutable IDs — safe for @Async.
        // Process crash after commit but before listener delivery is a known limitation (no outbox).
        try {
            eventPublisher.publishEvent(new StampCollectedEvent(
                    this,
                    UUID.randomUUID(),
                    saved.getId(),
                    cmd.userId(),
                    station.id(),
                    station.lineId(),
                    campaign.getId(),
                    saved.getCollectedAt(),
                    collectMethod
            ));
        } catch (Exception e) {
            log.error("[Collection] StampCollectedEvent publish failed userId={} stationId={}: {}",
                    cmd.userId(), station.id(), e.getMessage(), e);
            meterRegistry.counter("collection.stamp_collected.publish_failed").increment();
        }

        RbacTransactionCallbacks.afterCommit(() ->
                auditHelper.scheduleStampCollected(cmd.userId(), station.id(), campaign.getId(), design.getId()));

        return buildResult(saved, true, station, design, campaign, gpsResult, scanType);
    }

    /** @deprecated Use {@link #collect(CollectStampCommand)} — kept for legacy controller. */
    @Deprecated
    @Transactional
    public StampCollectView collectStamp(CollectStampCommand cmd) {
        CollectStampResultView result = collect(cmd);
        return StampCollectView.builder()
                .stampId(result.stamp().stampId())
                .stationId(result.stamp().stationId())
                .stationName(result.stamp().stationName())
                .lineId(result.stamp().lineId())
                .campaignId(result.stamp().campaignId())
                .stampDesignUrl(result.stamp().stampDesignUrl())
                .collectedAt(result.stamp().collectedAt())
                .isNew(result.isNew())
                .collectMethod(result.scan().scanType())
                .progress(result.progress())
                .build();
    }

    private CollectStampResultView buildResult(UserStamp userStamp, boolean isNew) {
        MetroStationView station = stationReadPort.getStationViewById(userStamp.getStationId());
        String lineName = lineReadPort.getLineById(station.lineId()).name();
        StampDesign design = stampDesignRepository.findById(userStamp.getStampDesignId()).orElse(null);
        ProgressView progress = collectionQueryService.computeProgress(
                userStamp.getUserId(), station.lineId(), userStamp.getCampaignId());
        return CollectStampResultView.builder()
                .stamp(CollectStampResultView.StampInfo.builder()
                        .stampId(userStamp.getId())
                        .stationId(userStamp.getStationId())
                        .stationName(station.name())
                        .lineName(lineName)
                        .lineId(station.lineId())
                        .campaignId(userStamp.getCampaignId())
                        .stampDesignUrl(design != null ? design.getImageUrl() : null)
                        .collectedAt(userStamp.getCollectedAt())
                        .build())
                .progress(progress)
                .scan(CollectStampResultView.ScanInfo.builder()
                        .scanType(userStamp.getSourceScanType())
                        .gpsDistanceMeters(userStamp.getGpsDistanceMeters())
                        .gpsAccuracyMeters(userStamp.getGpsAccuracyMeters())
                        .build())
                .isNew(isNew)
                .build();
    }

    private CollectStampResultView buildResult(
            UserStamp saved,
            boolean isNew,
            ResolvedStationView station,
            StampDesign design,
            Campaign campaign,
            GpsValidationService.GpsValidationResult gpsResult,
            String scanType
    ) {
        ProgressView progress = collectionQueryService.computeProgress(
                saved.getUserId(), station.lineId(), campaign.getId());
        return CollectStampResultView.builder()
                .stamp(CollectStampResultView.StampInfo.builder()
                        .stampId(saved.getId())
                        .stationId(station.id())
                        .stationName(station.name())
                        .lineName(station.lineName())
                        .lineId(station.lineId())
                        .campaignId(campaign.getId())
                        .stampDesignUrl(design.getImageUrl())
                        .collectedAt(saved.getCollectedAt())
                        .build())
                .progress(progress)
                .scan(CollectStampResultView.ScanInfo.builder()
                        .scanType(scanType)
                        .gpsDistanceMeters(gpsResult.distanceMeters())
                        .gpsAccuracyMeters(gpsResult.accuracyMeters())
                        .build())
                .isNew(isNew)
                .build();
    }

    private static void validateCommand(CollectStampCommand cmd) {
        if (cmd == null) {
            throw new InvalidRequestException("Missing command");
        }
        if (cmd.userId() == null) {
            throw new InvalidRequestException("Missing userId");
        }
        if (cmd.scanType() == null || cmd.scanType().isBlank()) {
            throw new InvalidRequestException("scanType is required");
        }
    }

    private static CollectMethod toCollectMethod(String scanType) {
        return "NFC".equals(scanType) ? CollectMethod.NFC : CollectMethod.QR;
    }

    private static String resolveDeviceFingerprint(CollectStampCommand cmd) {
        if (cmd.deviceFingerprint() != null && !cmd.deviceFingerprint().isBlank()) {
            return cmd.deviceFingerprint();
        }
        String platform = cmd.devicePlatform() != null ? cmd.devicePlatform() : "unknown";
        String version = cmd.appVersion() != null ? cmd.appVersion() : "unknown";
        return platform + ":" + version;
    }

    private static boolean isUserStampCollectUniqueViolation(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        if (msg == null) {
            return false;
        }
        return msg.contains("uq_user_stamps_collect")
                || (msg.contains("user_stamps") && msg.contains("user_id") && msg.contains("station_id"));
    }
}
