package metro.ExoticStamp.modules.collection.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.modules.collection.application.mapper.CampaignAppMapper;
import metro.ExoticStamp.modules.collection.application.view.StampDesignView;
import metro.ExoticStamp.modules.collection.config.CollectionProperties;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidRequestException;
import metro.ExoticStamp.modules.collection.domain.model.StampDesign;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StampDesignQueryService {

    private final StampDesignRepository stampDesignRepository;
    private final CampaignAppMapper campaignAppMapper;
    private final CollectionProperties collectionProperties;

    public PageResult<StampDesignView> list(int page, int size) {
        int safePage = Math.max(0, page);
        int capped = Math.min(Math.max(size, 1), collectionProperties.getMaxPageSize());
        PageResult<StampDesign> pageResult = stampDesignRepository.findAllNotDeletedPaged(safePage, capped);
        return PageResult.of(
                pageResult.content().stream().map(campaignAppMapper::toStampDesignView).toList(),
                pageResult.totalElements(),
                pageResult.totalPages(),
                pageResult.currentPage()
        );
    }

    public StampDesignView getById(UUID id) {
        StampDesign entity = stampDesignRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new InvalidRequestException("Stamp design not found: " + id));
        return campaignAppMapper.toStampDesignView(entity);
    }

    public List<StampDesignView> listByCampaignId(UUID campaignId) {
        return stampDesignRepository.findByCampaignIdOrderBySortOrderAsc(campaignId).stream()
                .filter(sd -> !sd.isDeleted())
                .map(campaignAppMapper::toStampDesignView)
                .toList();
    }
}
