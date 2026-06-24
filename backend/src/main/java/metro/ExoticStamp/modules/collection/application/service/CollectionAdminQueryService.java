package metro.ExoticStamp.modules.collection.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.modules.collection.application.mapper.CollectionAdminMapper;
import metro.ExoticStamp.modules.collection.application.view.AdminCampaignView;
import metro.ExoticStamp.modules.collection.application.view.AdminStampDesignView;
import metro.ExoticStamp.modules.collection.application.view.CollectionAdminStatsView;
import metro.ExoticStamp.modules.collection.config.CollectionProperties;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidRequestException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CollectionAdminQueryService {

    private final CampaignRepository campaignRepository;
    private final StampDesignRepository stampDesignRepository;
    private final UserStampRepository userStampRepository;
    private final CollectionAdminMapper collectionAdminMapper;
    private final CollectionProperties collectionProperties;

    public PageResult<AdminCampaignView> listCampaigns(int page, int size) {
        int safePage = Math.max(0, page);
        int capped = Math.min(Math.max(size, 1), collectionProperties.getMaxPageSize());
        PageResult<Campaign> pageResult = campaignRepository.findAllPaged(safePage, capped);
        List<AdminCampaignView> content = pageResult.content().stream()
                .map(collectionAdminMapper::toCampaignView)
                .toList();
        return PageResult.of(content, pageResult.totalElements(), pageResult.totalPages(), pageResult.currentPage());
    }

    public AdminCampaignView getCampaign(UUID id) {
        Campaign campaign = campaignRepository.findById(id).orElseThrow(() -> new CampaignNotFoundException(id));
        return collectionAdminMapper.toCampaignView(campaign);
    }

    public List<AdminStampDesignView> listStampDesignsForCampaign(UUID campaignId) {
        if (campaignRepository.findById(campaignId).isEmpty()) {
            throw new CampaignNotFoundException(campaignId);
        }
        List<StampDesign> list = stampDesignRepository.findByCampaignIdOrderBySortOrderAsc(campaignId);
        return list.stream().map(collectionAdminMapper::toStampDesignView).toList();
    }

    public AdminStampDesignView getStampDesign(UUID id) {
        StampDesign entity = stampDesignRepository.findById(id)
                .orElseThrow(() -> new InvalidRequestException("Stamp design not found: " + id));
        return collectionAdminMapper.toStampDesignView(entity);
    }

    public CollectionAdminStatsView getStats() {
        long total = userStampRepository.countAll();
        Map<UUID, Long> byCampaign = userStampRepository.countStampsByCampaignId();
        List<CollectionAdminStatsView.CampaignStampCountView> rows = new ArrayList<>();
        for (Map.Entry<UUID, Long> e : byCampaign.entrySet()) {
            rows.add(new CollectionAdminStatsView.CampaignStampCountView(e.getKey(), e.getValue()));
        }
        rows.sort(Comparator.comparing(CollectionAdminStatsView.CampaignStampCountView::campaignId, Comparator.nullsLast(Comparator.naturalOrder())));
        return CollectionAdminStatsView.builder()
                .totalStampsCollected(total)
                .stampsPerCampaign(rows)
                .build();
    }
}
