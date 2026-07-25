package metro.ExoticStamp.modules.collection.presentation.mapper;

import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.modules.collection.application.view.ActiveCampaignStationView;
import metro.ExoticStamp.modules.collection.application.view.ActiveCampaignView;
import metro.ExoticStamp.modules.collection.application.view.CampaignStationView;
import metro.ExoticStamp.modules.collection.application.view.CampaignView;
import metro.ExoticStamp.modules.collection.application.view.StampDesignView;
import metro.ExoticStamp.modules.collection.application.view.StampPreviewView;
import metro.ExoticStamp.modules.collection.presentation.dto.CampaignStatusApi;
import metro.ExoticStamp.modules.collection.presentation.dto.CampaignTypeApi;
import metro.ExoticStamp.modules.collection.presentation.dto.request.CreateCampaignRequest;
import metro.ExoticStamp.modules.collection.presentation.dto.request.UpdateCampaignRequest;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CampaignPresentationMapperTest {

    private final CampaignPresentationMapper mapper = new CampaignPresentationMapper();

    @Test
    void toCreateCommand_mapsEnumNames() {
        CreateCampaignRequest req = new CreateCampaignRequest();
        req.setCode("C1");
        req.setName("Campaign");
        req.setDisplayName("Display");
        req.setCampaignType(CampaignTypeApi.STANDARD);
        req.setStartAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        req.setEndAt(LocalDateTime.of(2026, 12, 31, 23, 59));
        req.setPriority(1);

        var cmd = mapper.toCreateCommand(req);

        assertEquals("C1", cmd.code());
        assertEquals("STANDARD", cmd.campaignType());
        assertEquals(1, cmd.priority());
    }

    @Test
    void toUpdateCommand_mapsStatusAndType() {
        UUID id = UUID.randomUUID();
        UpdateCampaignRequest req = new UpdateCampaignRequest();
        req.setStatus(CampaignStatusApi.ACTIVE);
        req.setCampaignType(CampaignTypeApi.STANDARD);
        req.setName("Updated");

        var cmd = mapper.toUpdateCommand(id, req);

        assertEquals(id, cmd.id());
        assertEquals("ACTIVE", cmd.status());
        assertEquals("Updated", cmd.name());
    }

    @Test
    void toCampaignPage_preservesPagination() {
        UUID id = UUID.randomUUID();
        CampaignView view = CampaignView.builder()
                .id(id)
                .code("C1")
                .name("N")
                .displayName("DN")
                .campaignType("STANDARD")
                .status("ACTIVE")
                .startAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .endAt(LocalDateTime.of(2026, 12, 31, 0, 0))
                .priority(0)
                .build();
        PageResult<CampaignView> page = PageResult.of(List.of(view), 1, 1, 0);

        var response = mapper.toCampaignPage(page);

        assertEquals(1, response.content().size());
        assertEquals(id, response.content().get(0).id());
        assertEquals("C1", response.content().get(0).code());
    }

    @Test
    void toActiveResponse_mapsStationsAndNullPreview() {
        UUID campaignId = UUID.randomUUID();
        ActiveCampaignView view = ActiveCampaignView.builder()
                .id(campaignId)
                .code("C1")
                .name("Summer")
                .displayName("Summer Promo")
                .campaignType("STANDARD")
                .priority(1)
                .startAt(LocalDateTime.of(2026, 6, 1, 0, 0))
                .endAt(LocalDateTime.of(2026, 8, 31, 0, 0))
                .stations(List.of(
                        ActiveCampaignStationView.builder()
                                .id(UUID.randomUUID())
                                .name("Central")
                                .displayName("Central Station")
                                .sortOrder(0)
                                .stampPreview(null)
                                .build(),
                        ActiveCampaignStationView.builder()
                                .id(UUID.randomUUID())
                                .name("North")
                                .displayName("North Station")
                                .sortOrder(1)
                                .stampPreview(StampPreviewView.builder()
                                        .id(UUID.randomUUID())
                                        .name("Stamp")
                                        .imageUrl("https://cdn/stamp.png")
                                        .previewImageUrl("https://cdn/preview.png")
                                        .rarity("RARE")
                                        .build())
                                .build()))
                .build();

        var response = mapper.toActiveResponse(view);

        assertEquals(2, response.stations().size());
        assertNull(response.stations().get(0).stampPreview());
        assertEquals("Stamp", response.stations().get(1).stampPreview().name());
    }

    @Test
    void toStampDesignPage_mapsFields() {
        UUID id = UUID.randomUUID();
        StampDesignView view = StampDesignView.builder()
                .id(id)
                .campaignId(UUID.randomUUID())
                .stationId(UUID.randomUUID())
                .name("Design")
                .imageUrl("https://cdn/img.png")
                .rarity("COMMON")
                .status("ACTIVE")
                .sortOrder(0)
                .build();
        PageResult<StampDesignView> page = PageResult.of(List.of(view), 1, 1, 0);

        var response = mapper.toStampDesignPage(page);

        assertEquals("Design", response.content().get(0).name());
        assertEquals("COMMON", response.content().get(0).rarity());
    }

    @Test
    void toResponse_campaignStationView() {
        UUID stationId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        var view = CampaignStationView.builder()
                .stationId(stationId)
                .name("Central")
                .displayName("Central Metro")
                .lineId(lineId)
                .sortOrder(2)
                .build();

        var response = mapper.toResponse(view);

        assertEquals(stationId, response.stationId());
        assertEquals(lineId, response.lineId());
        assertEquals(2, response.sortOrder());
    }
}
