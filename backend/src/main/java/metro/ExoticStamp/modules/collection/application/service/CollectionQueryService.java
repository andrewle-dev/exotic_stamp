package metro.ExoticStamp.modules.collection.application.service;



import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;

import metro.ExoticStamp.common.response.PageResponse;

import metro.ExoticStamp.modules.collection.application.mapper.UserStampAppMapper;

import metro.ExoticStamp.modules.collection.application.port.UserStampCachePort;

import metro.ExoticStamp.modules.collection.application.support.DefaultCampaignResolver;

import metro.ExoticStamp.modules.collection.application.view.CollectOutcomeStatus;
import metro.ExoticStamp.modules.collection.application.view.CollectStampResultView;
import metro.ExoticStamp.modules.collection.application.view.CollectStatusView;
import metro.ExoticStamp.modules.collection.application.view.ProgressView;

import metro.ExoticStamp.modules.collection.application.view.StampBookView;

import metro.ExoticStamp.modules.collection.application.view.UserStampView;

import metro.ExoticStamp.modules.collection.config.CollectionProperties;

import metro.ExoticStamp.modules.collection.domain.exception.InvalidRequestException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;

import metro.ExoticStamp.modules.collection.domain.model.StampDesign;

import metro.ExoticStamp.modules.collection.domain.model.UserStamp;

import metro.ExoticStamp.modules.collection.domain.model.UserStampSlice;

import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;

import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;

import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;

import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;

import metro.ExoticStamp.modules.metro.application.port.LineReadPort;

import metro.ExoticStamp.modules.metro.application.port.StationReadPort;

import metro.ExoticStamp.modules.metro.application.view.MetroStationView;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;



import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import java.util.function.Function;

import java.util.stream.Collectors;



@Slf4j

@Service

@RequiredArgsConstructor

@Transactional(readOnly = true)

public class CollectionQueryService {



    private final CampaignRepository campaignRepository;

    private final CampaignStationRepository campaignStationRepository;

    private final DefaultCampaignResolver defaultCampaignResolver;

    private final StampDesignRepository stampDesignRepository;

    private final UserStampRepository userStampRepository;

    private final StationReadPort stationReadPort;

    private final LineReadPort lineReadPort;

    private final UserStampCachePort cachePort;

    private final UserStampAppMapper userStampAppMapper;

    private final CollectionProperties collectionProperties;

    private final Clock clock;



    public PageResponse<UserStampView> getMyStamps(UUID userId, UUID lineId, int page, int size) {

        Campaign campaign = defaultCampaignResolver.resolveActiveGlobalDefault(lineId);

        UUID effectiveLineId = campaign.getLineId() != null ? campaign.getLineId() : lineId;

        int p = Math.max(0, page);

        int s = normalizeSize(size);



        Optional<PageResponse<UserStampView>> cached = cachePort.getUserStamps(userId, effectiveLineId, campaign.getId(), p, s);

        if (cached.isPresent()) {

            return cached.get();

        }



        UserStampSlice slice = userStampRepository.findByUserIdAndCampaignIdPaged(userId, campaign.getId(), p, s);

        List<UserStampView> mapped = mapUserStamps(slice.content());

        PageResponse<UserStampView> res = PageResponse.of(

                mapped,

                slice.totalElements(),

                slice.totalPages(),

                slice.page(),

                slice.size()

        );

        cachePort.putUserStamps(userId, effectiveLineId, campaign.getId(), p, s, res);

        return res;

    }



    /**
     * Read-only lookup for uncertain collect outcomes after client timeout.
     * Never creates stamps; only returns data scoped to {@code userId}.
     */
    public CollectStatusView getCollectStatus(UUID userId, UUID idempotencyKey) {
        if (userId == null) {
            throw new InvalidRequestException("Missing userId");
        }
        if (idempotencyKey == null) {
            throw new InvalidRequestException("idempotencyKey is required");
        }
        String key = idempotencyKey.toString();
        Optional<UserStamp> stampOpt = userStampRepository
                .findFirstByUserIdAndIdempotencyKeyOrderByCollectedAtDesc(userId, key);
        if (stampOpt.isEmpty()) {
            return CollectStatusView.builder()
                    .status(CollectOutcomeStatus.NOT_FOUND)
                    .build();
        }

        UserStamp stamp = stampOpt.get();
        LocalDateTime now = LocalDateTime.now(clock);
        Duration window = collectionProperties.getIdempotencyWindow();
        LocalDateTime windowStart = now.minus(window);
        CollectOutcomeStatus status = stamp.getCollectedAt() != null
                && !stamp.getCollectedAt().isBefore(windowStart)
                ? CollectOutcomeStatus.DUPLICATE
                : CollectOutcomeStatus.SUCCESS;

        MetroStationView station = stationReadPort.getStationViewById(stamp.getStationId());
        String lineName = lineReadPort.getLineById(station.lineId()).name();
        StampDesign design = stampDesignRepository.findById(stamp.getStampDesignId()).orElse(null);
        ProgressView progress = computeProgress(userId, station.lineId(), stamp.getCampaignId());

        return CollectStatusView.builder()
                .status(status)
                .stamp(CollectStampResultView.StampInfo.builder()
                        .stampId(stamp.getId())
                        .stationId(stamp.getStationId())
                        .stationName(station.name())
                        .lineName(lineName)
                        .lineId(station.lineId())
                        .campaignId(stamp.getCampaignId())
                        .stampDesignUrl(design != null ? design.getImageUrl() : null)
                        .collectedAt(stamp.getCollectedAt())
                        .build())
                .scan(CollectStampResultView.ScanInfo.builder()
                        .scanType(stamp.getSourceScanType())
                        .gpsDistanceMeters(stamp.getGpsDistanceMeters())
                        .gpsAccuracyMeters(stamp.getGpsAccuracyMeters())
                        .build())
                .progress(progress)
                .createdAt(stamp.getCreatedAt() != null ? stamp.getCreatedAt() : stamp.getCollectedAt())
                .resolvedAt(stamp.getCollectedAt())
                .build();
    }



    /** @deprecated Legacy signature with explicit campaignId. */

    @Deprecated

    public PageResponse<UserStampView> getMyStamps(UUID userId, UUID lineId, UUID campaignId, int page, int size) {

        if (campaignId != null) {

            Campaign campaign = campaignRepository.findById(campaignId)

                    .orElseThrow(() -> new metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException(campaignId));

            UUID effectiveLineId = campaign.getLineId() != null ? campaign.getLineId() : lineId;

            int p = Math.max(0, page);

            int s = normalizeSize(size);

            UserStampSlice slice = userStampRepository.findByUserIdAndCampaignIdPaged(userId, campaign.getId(), p, s);

            return PageResponse.of(

                    mapUserStamps(slice.content()),

                    slice.totalElements(),

                    slice.totalPages(),

                    slice.page(),

                    slice.size()

            );

        }

        return getMyStamps(userId, lineId, page, size);

    }



    public ProgressView getMyProgress(UUID userId, UUID lineId, UUID campaignId) {

        Campaign campaign = campaignId != null

                ? campaignRepository.findById(campaignId)

                    .orElseThrow(() -> new metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException(campaignId))

                : defaultCampaignResolver.resolveActiveGlobalDefault(lineId);

        UUID effectiveLineId = campaign.getLineId() != null ? campaign.getLineId() : lineId;



        Optional<ProgressView> cached = cachePort.getUserProgress(userId, effectiveLineId);

        if (cached.isPresent()) {

            return cached.get();

        }



        ProgressView computed = computeProgress(userId, effectiveLineId, campaign.getId());

        cachePort.putUserProgress(userId, effectiveLineId, computed);

        return computed;

    }



    public PageResponse<UserStampView> getMyHistory(UUID userId, int page, int size) {

        int p = Math.max(0, page);

        int s = normalizeSize(size);



        Optional<PageResponse<UserStampView>> cached = cachePort.getUserHistory(userId, p, s);

        if (cached.isPresent()) {

            return cached.get();

        }



        UserStampSlice slice = userStampRepository.findByUserIdPaged(userId, p, s);

        List<UserStampView> mapped = mapUserStamps(slice.content());

        PageResponse<UserStampView> res = PageResponse.of(

                mapped,

                slice.totalElements(),

                slice.totalPages(),

                slice.page(),

                slice.size()

        );

        cachePort.putUserHistory(userId, p, s, res);

        return res;

    }



    public StampBookView getStampBook(UUID userId, UUID lineId) {

        Campaign campaign = defaultCampaignResolver.resolveActiveGlobalDefault(lineId);

        UUID effectiveLineId = campaign.getLineId() != null ? campaign.getLineId() : lineId;



        Optional<StampBookView> cached = cachePort.getStampBook(userId, effectiveLineId, campaign.getId());

        if (cached.isPresent()) {

            return cached.get();

        }



        StampBookView view = buildStampBook(userId, effectiveLineId, campaign);

        cachePort.putStampBook(userId, effectiveLineId, campaign.getId(), view);

        return view;

    }



    /** @deprecated Legacy signature with explicit campaignId. */

    @Deprecated

    public StampBookView getStampBook(UUID userId, UUID lineId, UUID campaignId) {

        if (campaignId != null) {

            Campaign campaign = campaignRepository.findById(campaignId)

                    .orElseThrow(() -> new metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException(campaignId));

            UUID effectiveLineId = campaign.getLineId() != null ? campaign.getLineId() : lineId;

            return buildStampBook(userId, effectiveLineId, campaign);

        }

        return getStampBook(userId, lineId);

    }



    /**
     * Progress for one campaign (Home / stamp-book / collect response).
     *
     * <p>Scope (must stay aligned):
     * <ul>
     *   <li>{@code collected} — distinct stations the user collected in this campaign</li>
     *   <li>{@code total} — ACTIVE non-deleted stamp designs in this campaign
     *       (not {@code campaign_stations} row count alone, and not line station count)</li>
     * </ul>
     * Historical stamps beyond current ACTIVE designs may yield {@code collected > total};
     * percentage is capped at 100 in that case.
     */
    public ProgressView computeProgress(UUID userId, UUID lineId, UUID campaignId) {

        long collected = userStampRepository.countDistinctStationsByUserIdAndCampaignId(userId, campaignId);

        long total = stampDesignRepository.countActiveByCampaignId(campaignId);

        int pct = total <= 0
                ? 0
                : (int) Math.min(100, Math.floor((collected * 100.0) / total));

        return ProgressView.builder()

                .lineId(lineId)

                .collected(collected)

                .total(total)

                .percentage(pct)

                .build();

    }



    private StampBookView buildStampBook(UUID userId, UUID lineId, Campaign campaign) {

        List<UUID> stationIds = campaignStationRepository.findStationIdsByCampaignId(campaign.getId());

        List<MetroStationView> campaignStations = stationReadPort.listStationViewsByIds(new HashSet<>(stationIds));

        campaignStations = campaignStations.stream()

                .sorted(Comparator.comparing(MetroStationView::sequence, Comparator.nullsLast(Comparator.naturalOrder())))

                .toList();



        List<UserStamp> collected = userStampRepository.findByUserIdAndCampaignId(userId, campaign.getId());

        Set<UUID> collectedStationIds = collected.stream().map(UserStamp::getStationId).collect(Collectors.toSet());

        Map<UUID, UserStamp> collectedByStation = collected.stream()
                .collect(Collectors.toMap(UserStamp::getStationId, Function.identity(), (a, b) -> a));



        List<StampDesign> designs = stampDesignRepository.findActiveByCampaignIdAndStationIdIn(campaign.getId(), stationIds);

        Map<UUID, StampDesign> designByStation = designs.stream()

                .filter(d -> d.getStationId() != null)

                .collect(Collectors.toMap(StampDesign::getStationId, Function.identity(), (a, b) -> a));



        String lineName = lineReadPort.getLineById(lineId).name();



        List<StampBookView.StationCellView> stations = new ArrayList<>();

        for (MetroStationView s : campaignStations) {

            boolean isCollected = collectedStationIds.contains(s.id());

            StampDesign design = designByStation.get(s.id());

            UserStamp userStamp = collectedByStation.get(s.id());

            stations.add(StampBookView.StationCellView.builder()

                    .stationId(s.id())

                    .stationName(s.name())

                    .sequence(s.sequence())

                    .collected(isCollected)

                    .stampDesignUrl(design != null ? design.getImageUrl() : null)

                    .stampDesignName(design != null ? design.getName() : null)

                    .stampDesignDescription(design != null ? design.getDescription() : null)

                    .rarity(design != null && design.getRarity() != null ? design.getRarity().name() : null)

                    .collectedAt(userStamp != null ? userStamp.getCollectedAt() : null)

                    .build());

        }



        ProgressView progress = computeProgress(userId, lineId, campaign.getId());



        return StampBookView.builder()

                .lineId(lineId)

                .lineName(lineName)

                .campaignId(campaign.getId())

                .campaignName(campaign.getDisplayName())

                .stations(stations)

                .progress(progress)

                .build();

    }



    private int normalizeSize(int size) {

        int def = collectionProperties.getDefaultPageSize();

        int max = collectionProperties.getMaxPageSize();

        if (size <= 0) {

            return def;

        }

        return Math.min(size, max);

    }



    private List<UserStampView> mapUserStamps(List<UserStamp> stamps) {

        if (stamps.isEmpty()) {

            return List.of();

        }

        Set<UUID> stationIds = stamps.stream().map(UserStamp::getStationId).collect(Collectors.toSet());

        Set<UUID> designIds = stamps.stream().map(UserStamp::getStampDesignId).collect(Collectors.toSet());



        Map<UUID, MetroStationView> stationById = stationReadPort.listStationViewsByIds(stationIds).stream()

                .collect(Collectors.toMap(MetroStationView::id, Function.identity(), (a, b) -> a));

        Map<UUID, StampDesign> designById = stampDesignRepository.findAllByIdIn(designIds).stream()

                .collect(Collectors.toMap(StampDesign::getId, Function.identity(), (a, b) -> a));



        List<UserStampView> res = new ArrayList<>(stamps.size());

        for (UserStamp us : stamps) {

            MetroStationView station = stationById.get(us.getStationId());

            StampDesign design = designById.get(us.getStampDesignId());

            if (station == null) {

                log.warn("[CollectionQuery] missing station view for stationId={}", us.getStationId());

                continue;

            }

            res.add(userStampAppMapper.toUserStampResponse(us, station, design));

        }

        return res;

    }

}

