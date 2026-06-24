package metro.ExoticStamp.modules.collection.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.modules.collection.application.mapper.CampaignAppMapper;
import metro.ExoticStamp.modules.collection.application.view.CampaignView;
import metro.ExoticStamp.modules.collection.config.CollectionProperties;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaignQueryService {

    private final CampaignRepository campaignRepository;
    private final CampaignAppMapper campaignAppMapper;
    private final CollectionProperties collectionProperties;

    public PageResult<CampaignView> list(int page, int size) {
        int safePage = Math.max(0, page);
        int capped = Math.min(Math.max(size, 1), collectionProperties.getMaxPageSize());
        PageResult<Campaign> pageResult = campaignRepository.findAllNotDeletedPaged(safePage, capped);
        return PageResult.of(
                pageResult.content().stream().map(campaignAppMapper::toCampaignView).toList(),
                pageResult.totalElements(),
                pageResult.totalPages(),
                pageResult.currentPage()
        );
    }

    public CampaignView getById(UUID id) {
        Campaign campaign = campaignRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new CampaignNotFoundException(id));
        return campaignAppMapper.toCampaignView(campaign);
    }
}
