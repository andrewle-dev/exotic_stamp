package metro.ExoticStamp.modules.collection.application;

import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.modules.collection.application.mapper.CollectionAdminMapper;
import metro.ExoticStamp.modules.collection.application.service.CollectionAdminQueryService;
import metro.ExoticStamp.modules.collection.application.view.AdminCampaignView;
import metro.ExoticStamp.modules.collection.config.CollectionProperties;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import metro.ExoticStamp.modules.collection.domain.model.CampaignType;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.StampDesignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CollectionAdminQueryServiceTest {

    @Mock
    private CampaignRepository campaignRepository;
    @Mock
    private StampDesignRepository stampDesignRepository;
    @Mock
    private UserStampRepository userStampRepository;
    @Mock
    private CollectionProperties collectionProperties;

    @Test
    void listCampaigns_clampsNegativePageAndSize() {
        CollectionAdminQueryService service = new CollectionAdminQueryService(
                campaignRepository,
                stampDesignRepository,
                userStampRepository,
                new CollectionAdminMapper(),
                collectionProperties
        );

        when(collectionProperties.getMaxPageSize()).thenReturn(50);

        Campaign campaign = Campaign.builder()
                .id(UUID.randomUUID())
                .lineId(UUID.randomUUID())
                .code("C1")
                .name("Campaign 1")
                .displayName("Campaign 1")
                .campaignType(CampaignType.STANDARD)
                .status(CampaignStatus.ACTIVE)
                .priority(0)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(1))
                .isDefault(false)
                .build();
        when(campaignRepository.findAllPaged(0, 50))
                .thenReturn(PageResult.of(List.of(campaign), 1, 1, 0));

        PageResult<AdminCampaignView> result = service.listCampaigns(-3, 999);

        assertEquals(1, result.content().size());
        verify(campaignRepository).findAllPaged(0, 50);
    }
}
