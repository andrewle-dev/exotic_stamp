package metro.ExoticStamp.modules.collection.application;

import metro.ExoticStamp.modules.auth.application.AuditLogService;
import metro.ExoticStamp.modules.collection.application.command.CreateCampaignCommand;
import metro.ExoticStamp.modules.collection.application.command.UpdateCampaignCommand;
import metro.ExoticStamp.modules.collection.application.mapper.CampaignAppMapper;
import metro.ExoticStamp.modules.collection.application.service.CampaignCommandService;
import metro.ExoticStamp.modules.collection.application.support.CampaignAuditHelper;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignArchivedException;
import metro.ExoticStamp.modules.collection.domain.exception.CampaignCodeDuplicateException;
import metro.ExoticStamp.modules.collection.domain.exception.InvalidRequestException;
import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import metro.ExoticStamp.modules.collection.domain.model.CampaignType;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignStationRepository;
import metro.ExoticStamp.modules.collection.domain.service.CampaignDomainService;
import metro.ExoticStamp.modules.rbac.application.support.RbacSecurityContextHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CampaignCommandServiceTest {

    @Mock private CampaignRepository campaignRepository;
    @Mock private CampaignStationRepository campaignStationRepository;
    @Mock private AuditLogService auditLogService;
    @Mock private RbacSecurityContextHelper securityContextHelper;

    private CampaignCommandService service;
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        CampaignDomainService domainService = new CampaignDomainService();
        CampaignAuditHelper auditHelper = new CampaignAuditHelper(auditLogService, securityContextHelper);
        service = new CampaignCommandService(
                campaignRepository,
                campaignStationRepository,
                domainService,
                new CampaignAppMapper(),
                auditHelper,
                clock
        );
    }

    @Test
    void create_success() {
        when(campaignRepository.existsByCode("SUMMER")).thenReturn(false);
        when(campaignRepository.save(any())).thenAnswer(inv -> {
            Campaign c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        var view = service.create(new CreateCampaignCommand(
                "SUMMER", "Summer", "Summer Display", "desc", CampaignType.SEASONAL.name(),
                LocalDateTime.now(clock), LocalDateTime.now(clock).plusMonths(3),
                "https://banner", "https://thumb", 5));

        assertEquals("SUMMER", view.code());
        assertEquals("DRAFT", view.status());
    }

    @Test
    void create_duplicateCode() {
        when(campaignRepository.existsByCode("DUP")).thenReturn(true);
        assertThrows(CampaignCodeDuplicateException.class, () -> service.create(new CreateCampaignCommand(
                "DUP", "N", null, null, null,
                LocalDateTime.now(clock), LocalDateTime.now(clock).plusDays(1),
                null, null, null)));
    }

    @Test
    void create_invalidDateRange() {
        assertThrows(InvalidRequestException.class, () -> service.create(new CreateCampaignCommand(
                "X", "N", null, null, null,
                LocalDateTime.now(clock), LocalDateTime.now(clock).minusDays(1),
                null, null, null)));
    }

    @Test
    void activate_campaign() {
        UUID id = UUID.randomUUID();
        Campaign campaign = Campaign.builder()
                .id(id).code("C1").name("C").displayName("C")
                .campaignType(CampaignType.STANDARD).status(CampaignStatus.DRAFT)
                .startAt(LocalDateTime.now(clock).minusDays(1))
                .endAt(LocalDateTime.now(clock).plusDays(30))
                .priority(0).isDefault(false).build();
        when(campaignRepository.findByIdNotDeleted(id)).thenReturn(Optional.of(campaign));
        when(campaignStationRepository.countByCampaignId(id)).thenReturn(1);
        when(campaignRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var view = service.update(new UpdateCampaignCommand(
                id, null, null, null, null, null, CampaignStatus.ACTIVE.name(),
                null, null, null, null, null));
        assertEquals("ACTIVE", view.status());
    }

    @Test
    void archive_campaign() {
        UUID id = UUID.randomUUID();
        Campaign campaign = Campaign.builder()
                .id(id).code("C1").name("C").displayName("C")
                .campaignType(CampaignType.STANDARD).status(CampaignStatus.ACTIVE)
                .startAt(LocalDateTime.now(clock).minusDays(1))
                .endAt(LocalDateTime.now(clock).plusDays(30))
                .priority(0).isDefault(false).build();
        when(campaignRepository.findByIdNotDeleted(id)).thenReturn(Optional.of(campaign));
        when(campaignRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var view = service.update(new UpdateCampaignCommand(
                id, null, null, null, null, null, CampaignStatus.ARCHIVED.name(),
                null, null, null, null, null));
        assertEquals("ARCHIVED", view.status());
    }

    @Test
    void archive_immutable() {
        UUID id = UUID.randomUUID();
        Campaign campaign = Campaign.builder()
                .id(id).code("C1").name("C").displayName("C")
                .campaignType(CampaignType.STANDARD).status(CampaignStatus.ARCHIVED)
                .startAt(LocalDateTime.now(clock).minusDays(1))
                .endAt(LocalDateTime.now(clock).plusDays(30))
                .priority(0).isDefault(false).build();
        when(campaignRepository.findByIdNotDeleted(id)).thenReturn(Optional.of(campaign));

        assertThrows(CampaignArchivedException.class, () -> service.update(new UpdateCampaignCommand(
                id, "NEW", null, null, null, null, CampaignStatus.ACTIVE.name(),
                null, null, null, null, null)));
    }

    @Test
    void activate_withoutStations_rejected() {
        UUID id = UUID.randomUUID();
        Campaign campaign = Campaign.builder()
                .id(id).code("C1").name("C").displayName("C")
                .campaignType(CampaignType.STANDARD).status(CampaignStatus.DRAFT)
                .startAt(LocalDateTime.now(clock).minusDays(1))
                .endAt(LocalDateTime.now(clock).plusDays(30))
                .priority(0).isDefault(false).build();
        when(campaignRepository.findByIdNotDeleted(id)).thenReturn(Optional.of(campaign));
        when(campaignStationRepository.countByCampaignId(id)).thenReturn(0);

        assertThrows(InvalidRequestException.class, () -> service.update(new UpdateCampaignCommand(
                id, null, null, null, null, null, CampaignStatus.ACTIVE.name(),
                null, null, null, null, null)));
    }

    @Test
    void update_duplicateCode_rejected() {
        UUID id = UUID.randomUUID();
        Campaign campaign = Campaign.builder()
                .id(id).code("OLD").name("C").displayName("C")
                .campaignType(CampaignType.STANDARD).status(CampaignStatus.DRAFT)
                .startAt(LocalDateTime.now(clock).minusDays(1))
                .endAt(LocalDateTime.now(clock).plusDays(30))
                .priority(0).isDefault(false).build();
        when(campaignRepository.findByIdNotDeleted(id)).thenReturn(Optional.of(campaign));
        when(campaignRepository.existsByCodeAndIdNot("NEW", id)).thenReturn(true);

        assertThrows(CampaignCodeDuplicateException.class, () -> service.update(new UpdateCampaignCommand(
                id, "NEW", null, null, null, null, null,
                null, null, null, null, null)));
    }

    @Test
    void create_negativePriority_rejected() {
        assertThrows(InvalidRequestException.class, () -> service.create(new CreateCampaignCommand(
                "X", "N", null, null, null,
                LocalDateTime.now(clock), LocalDateTime.now(clock).plusDays(1),
                null, null, -1)));
    }

    @Test
    void update_changesCampaignType() {
        UUID id = UUID.randomUUID();
        Campaign campaign = Campaign.builder()
                .id(id).code("C1").name("C").displayName("C")
                .campaignType(CampaignType.STANDARD).status(CampaignStatus.DRAFT)
                .startAt(LocalDateTime.now(clock).minusDays(1))
                .endAt(LocalDateTime.now(clock).plusDays(30))
                .priority(0).isDefault(false).build();
        when(campaignRepository.findByIdNotDeleted(id)).thenReturn(Optional.of(campaign));
        when(campaignRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var view = service.update(new UpdateCampaignCommand(
                id, null, null, null, null, CampaignType.SEASONAL.name(), null,
                null, null, null, null, null));

        assertEquals("SEASONAL", view.campaignType());
    }

    @Test
    void softDelete() {
        UUID id = UUID.randomUUID();
        Campaign campaign = Campaign.builder()
                .id(id).code("C1").name("C").displayName("C")
                .campaignType(CampaignType.STANDARD).status(CampaignStatus.DRAFT)
                .startAt(LocalDateTime.now(clock).minusDays(1))
                .endAt(LocalDateTime.now(clock).plusDays(30))
                .priority(0).isDefault(true).build();
        when(campaignRepository.findByIdNotDeleted(id)).thenReturn(Optional.of(campaign));
        when(campaignRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.softDelete(id);
        assertNotNull(campaign.getDeletedAt());
        assertTrue(campaign.isDefault()); // historical default preserved
        verify(campaignRepository).save(campaign);
    }

    @Test
    void create_nullCampaignType_defaultsStandard() {
        when(campaignRepository.existsByCode("STD")).thenReturn(false);
        when(campaignRepository.save(any())).thenAnswer(inv -> {
            Campaign c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        var view = service.create(new CreateCampaignCommand(
                "STD", "Standard", null, null, null,
                LocalDateTime.now(clock), LocalDateTime.now(clock).plusDays(1),
                null, null, 0));

        assertEquals("STANDARD", view.campaignType());
    }

    @Test
    void create_blankDisplayName_usesName() {
        when(campaignRepository.existsByCode("N1")).thenReturn(false);
        when(campaignRepository.save(any())).thenAnswer(inv -> {
            Campaign c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        var view = service.create(new CreateCampaignCommand(
                "N1", "Campaign Name", "   ", null, CampaignType.STANDARD.name(),
                LocalDateTime.now(clock), LocalDateTime.now(clock).plusDays(1),
                null, null, 0));

        assertEquals("Campaign Name", view.displayName());
    }

    @Test
    void update_draftStatusChange() {
        UUID id = UUID.randomUUID();
        Campaign campaign = Campaign.builder()
                .id(id).code("C1").name("C").displayName("C")
                .campaignType(CampaignType.STANDARD).status(CampaignStatus.ACTIVE)
                .startAt(LocalDateTime.now(clock).minusDays(1))
                .endAt(LocalDateTime.now(clock).plusDays(30))
                .priority(0).isDefault(false).build();
        when(campaignRepository.findByIdNotDeleted(id)).thenReturn(Optional.of(campaign));
        when(campaignRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var view = service.update(new UpdateCampaignCommand(
                id, null, null, null, null, null, CampaignStatus.DRAFT.name(),
                null, null, null, null, null));

        assertEquals("DRAFT", view.status());
    }

    @Test
    void update_notFound() {
        UUID id = UUID.randomUUID();
        when(campaignRepository.findByIdNotDeleted(id)).thenReturn(Optional.empty());
        assertThrows(metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException.class, () ->
                service.update(new UpdateCampaignCommand(
                        id, null, null, null, null, null, null,
                        null, null, null, null, null)));
    }

    @Test
    void softDelete_notFound() {
        UUID id = UUID.randomUUID();
        when(campaignRepository.findByIdNotDeleted(id)).thenReturn(Optional.empty());
        assertThrows(metro.ExoticStamp.modules.collection.domain.exception.CampaignNotFoundException.class,
                () -> service.softDelete(id));
    }

    @Test
    void update_negativePriority_rejected() {
        UUID id = UUID.randomUUID();
        Campaign campaign = Campaign.builder()
                .id(id).code("C1").name("C").displayName("C")
                .campaignType(CampaignType.STANDARD).status(CampaignStatus.DRAFT)
                .startAt(LocalDateTime.now(clock).minusDays(1))
                .endAt(LocalDateTime.now(clock).plusDays(30))
                .priority(0).isDefault(false).build();
        when(campaignRepository.findByIdNotDeleted(id)).thenReturn(Optional.of(campaign));

        assertThrows(InvalidRequestException.class, () -> service.update(new UpdateCampaignCommand(
                id, null, null, null, null, null, null,
                null, null, null, null, -5)));
    }

    @Test
    void update_partialBannerAndDates() {
        UUID id = UUID.randomUUID();
        Campaign campaign = Campaign.builder()
                .id(id).code("C1").name("C").displayName("C")
                .campaignType(CampaignType.STANDARD).status(CampaignStatus.DRAFT)
                .startAt(LocalDateTime.now(clock).minusDays(1))
                .endAt(LocalDateTime.now(clock).plusDays(30))
                .priority(0).isDefault(false).build();
        when(campaignRepository.findByIdNotDeleted(id)).thenReturn(Optional.of(campaign));
        when(campaignRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        LocalDateTime newEnd = LocalDateTime.now(clock).plusDays(60);
        var view = service.update(new UpdateCampaignCommand(
                id, null, null, null, "New desc", null, null,
                null, newEnd, "https://banner", "https://thumb", null));

        assertEquals("New desc", view.description());
        assertEquals(newEnd, view.endAt());
        assertEquals("https://banner", view.bannerImageUrl());
    }
}
