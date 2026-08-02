package metro.ExoticStamp.modules.reward.application.service;

import metro.ExoticStamp.modules.reward.application.command.CreatePartnerCommand;
import metro.ExoticStamp.modules.reward.application.command.CreateRewardCommand;
import metro.ExoticStamp.modules.reward.application.command.UpdatePartnerCommand;
import metro.ExoticStamp.modules.reward.application.command.UpdateRewardCommand;
import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.domain.exception.InvalidMilestoneStateException;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneArchivedException;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.PartnerAlreadyActiveException;
import metro.ExoticStamp.modules.reward.domain.exception.PartnerAlreadyInactiveException;
import metro.ExoticStamp.modules.reward.domain.exception.PartnerNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.RewardAlreadyActiveException;
import metro.ExoticStamp.modules.reward.domain.exception.RewardAlreadyInactiveException;
import metro.ExoticStamp.modules.reward.domain.exception.RewardNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.VoucherCodeExhaustedException;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.Partner;
import metro.ExoticStamp.modules.reward.domain.model.Reward;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import metro.ExoticStamp.modules.reward.domain.repository.PartnerRepository;
import metro.ExoticStamp.modules.reward.domain.repository.RewardRepository;
import metro.ExoticStamp.modules.reward.domain.repository.VoucherPoolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminRewardCommandServiceTest {

    @Mock private PartnerRepository partnerRepository;
    @Mock private MilestoneRepository milestoneRepository;
    @Mock private RewardRepository rewardRepository;
    @Mock private VoucherPoolRepository voucherPoolRepository;

    private AdminRewardCommandService service;

    @BeforeEach
    void setUp() {
        service = new AdminRewardCommandService(
                partnerRepository,
                milestoneRepository,
                rewardRepository,
                voucherPoolRepository,
                new RewardAppMapper());
    }

    @Test
    void createPartner_persistsActivePartner() {
        when(partnerRepository.save(any())).thenAnswer(inv -> {
            Partner p = inv.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });

        var view = service.createPartner(new CreatePartnerCommand(
                "Highland", "https://cdn/logo.png", "https://cdn/banner.png",
                "partner@test.com", null, null));

        assertEquals("Highland", view.name());
        assertTrue(view.active());
        ArgumentCaptor<Partner> captor = ArgumentCaptor.forClass(Partner.class);
        verify(partnerRepository).save(captor.capture());
        assertTrue(captor.getValue().isActive());
    }

    @Test
    void updatePartner_partialFields() {
        UUID id = UUID.randomUUID();
        Partner existing = Partner.builder().name("Old").active(true).build();
        existing.setId(id);
        when(partnerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(partnerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var view = service.updatePartner(new UpdatePartnerCommand(id, "New Name", null, null, null, null, null));

        assertEquals("New Name", view.name());
    }

    @Test
    void updatePartner_notFound() {
        UUID id = UUID.randomUUID();
        when(partnerRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(PartnerNotFoundException.class, () ->
                service.updatePartner(new UpdatePartnerCommand(id, "X", null, null, null, null, null)));
    }

    @Test
    void activatePartner_alreadyActive_throws() {
        UUID id = UUID.randomUUID();
        Partner p = Partner.builder().name("P").active(true).build();
        p.setId(id);
        when(partnerRepository.findById(id)).thenReturn(Optional.of(p));

        assertThrows(PartnerAlreadyActiveException.class, () -> service.activatePartner(id));
    }

    @Test
    void deactivatePartner_alreadyInactive_throws() {
        UUID id = UUID.randomUUID();
        Partner p = Partner.builder().name("P").active(false).build();
        p.setId(id);
        when(partnerRepository.findById(id)).thenReturn(Optional.of(p));

        assertThrows(PartnerAlreadyInactiveException.class, () -> service.deactivatePartner(id));
    }

    @Test
    void activatePartner_inactive_becomesActive() {
        UUID id = UUID.randomUUID();
        Partner p = Partner.builder().name("P").active(false).build();
        p.setId(id);
        when(partnerRepository.findById(id)).thenReturn(Optional.of(p));
        when(partnerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertTrue(service.activatePartner(id).active());
    }

    @Test
    void createReward_archivedMilestone_rejected() {
        UUID milestoneId = UUID.randomUUID();
        Milestone m = Milestone.builder()
                .campaignId(UUID.randomUUID())
                .code("M1")
                .stampsRequired(3)
                .name("M")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("T")
                .status(MilestoneStatus.ARCHIVED)
                .sortOrder(0)
                .build();
        m.setId(milestoneId);
        when(milestoneRepository.findByIdNotDeleted(milestoneId)).thenReturn(Optional.of(m));

        assertThrows(MilestoneArchivedException.class, () -> service.createReward(
                new CreateRewardCommand(milestoneId, null, "VOUCHER", "Reward", null, null, 30, 100)));
    }

    @Test
    void createReward_invalidRewardType_rejected() {
        UUID milestoneId = UUID.randomUUID();
        stubActiveMilestone(milestoneId);

        assertThrows(InvalidMilestoneStateException.class, () -> service.createReward(
                new CreateRewardCommand(milestoneId, null, "NOT_A_TYPE", "Reward", null, null, 30, 100)));
    }

    @Test
    void createReward_unsupportedRewardType_rejected() {
        UUID milestoneId = UUID.randomUUID();
        stubActiveMilestone(milestoneId);

        assertThrows(InvalidMilestoneStateException.class, () -> service.createReward(
                new CreateRewardCommand(milestoneId, null, "PHYSICAL_GIFT", "Reward", null, null, 30, 100)));
    }

    @Test
    void createReward_missingPartner_rejected() {
        UUID milestoneId = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();
        stubActiveMilestone(milestoneId);
        when(partnerRepository.existsById(partnerId)).thenReturn(false);

        assertThrows(PartnerNotFoundException.class, () -> service.createReward(
                new CreateRewardCommand(milestoneId, partnerId, "VOUCHER", "Reward", null, null, 30, 100)));
    }

    @Test
    void createReward_success() {
        UUID milestoneId = UUID.randomUUID();
        stubActiveMilestone(milestoneId);
        when(rewardRepository.save(any())).thenAnswer(inv -> {
            Reward r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        var view = service.createReward(new CreateRewardCommand(
                milestoneId, null, "VOUCHER", "  Voucher  ", "desc",
                BigDecimal.TEN, 30, 50));

        assertEquals("  Voucher  ", view.name());
        assertEquals(RewardType.VOUCHER, view.rewardType());
        assertTrue(view.active());
    }

    @Test
    void updateReward_unknownMilestone_rejected() {
        UUID rewardId = UUID.randomUUID();
        UUID newMilestoneId = UUID.randomUUID();
        Reward r = Reward.builder()
                .milestoneId(UUID.randomUUID())
                .rewardType(RewardType.VOUCHER)
                .name("R")
                .issuedCount(0)
                .active(true)
                .build();
        r.setId(rewardId);
        when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(r));
        when(milestoneRepository.existsById(newMilestoneId)).thenReturn(false);

        assertThrows(MilestoneNotFoundException.class, () -> service.updateReward(
                new UpdateRewardCommand(rewardId, newMilestoneId, null, null, null, null, null, null, null)));
    }

    @Test
    void activateReward_alreadyActive_throws() {
        UUID id = UUID.randomUUID();
        Reward r = Reward.builder()
                .milestoneId(UUID.randomUUID())
                .rewardType(RewardType.VOUCHER)
                .name("R")
                .issuedCount(0)
                .active(true)
                .build();
        r.setId(id);
        when(rewardRepository.findById(id)).thenReturn(Optional.of(r));

        assertThrows(RewardAlreadyActiveException.class, () -> service.activateReward(id));
    }

    @Test
    void deactivateReward_alreadyInactive_throws() {
        UUID id = UUID.randomUUID();
        Reward r = Reward.builder()
                .milestoneId(UUID.randomUUID())
                .rewardType(RewardType.VOUCHER)
                .name("R")
                .issuedCount(0)
                .active(false)
                .build();
        r.setId(id);
        when(rewardRepository.findById(id)).thenReturn(Optional.of(r));

        assertThrows(RewardAlreadyInactiveException.class, () -> service.deactivateReward(id));
    }

    @Test
    void bulkUploadVouchers_skipsBlankCodes() {
        UUID rewardId = UUID.randomUUID();
        when(rewardRepository.existsById(rewardId)).thenReturn(true);
        when(voucherPoolRepository.saveAll(any())).thenAnswer(inv -> (List<VoucherPool>) inv.getArgument(0));

        int count = service.bulkUploadVouchers(rewardId, Arrays.asList(" ABC ", "", null, "XYZ"));

        assertEquals(2, count);
    }

    @Test
    void bulkUploadVouchers_emptyList_returnsZero() {
        UUID rewardId = UUID.randomUUID();
        when(rewardRepository.existsById(rewardId)).thenReturn(true);

        assertEquals(0, service.bulkUploadVouchers(rewardId, List.of()));
        verify(voucherPoolRepository, never()).saveAll(any());
    }

    @Test
    void bulkUploadVouchers_rewardNotFound() {
        UUID rewardId = UUID.randomUUID();
        when(rewardRepository.existsById(rewardId)).thenReturn(false);

        assertThrows(RewardNotFoundException.class, () ->
                service.bulkUploadVouchers(rewardId, List.of("CODE")));
    }

    @Test
    void bulkUploadVouchers_duplicateCode_mapsToExhausted() {
        UUID rewardId = UUID.randomUUID();
        when(rewardRepository.existsById(rewardId)).thenReturn(true);
        when(voucherPoolRepository.saveAll(any())).thenThrow(
                new DataIntegrityViolationException("dup", new RuntimeException("uq_voucher_pool_code violated")));

        assertThrows(VoucherCodeExhaustedException.class, () ->
                service.bulkUploadVouchers(rewardId, List.of("DUP")));
    }

    @Test
    void createReward_milestoneNotFound() {
        UUID milestoneId = UUID.randomUUID();
        when(milestoneRepository.findByIdNotDeleted(milestoneId)).thenReturn(Optional.empty());

        assertThrows(MilestoneNotFoundException.class, () -> service.createReward(
                new CreateRewardCommand(milestoneId, null, "VOUCHER", "Reward", null, null, 30, 100)));
    }

    @Test
    void createReward_digitalSticker_supported() {
        UUID milestoneId = UUID.randomUUID();
        stubActiveMilestone(milestoneId);
        when(rewardRepository.save(any())).thenAnswer(inv -> {
            Reward r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        var view = service.createReward(new CreateRewardCommand(
                milestoneId, null, "DIGITAL_STICKER", "Sticker", null, null, null, null));

        assertEquals(RewardType.DIGITAL_STICKER, view.rewardType());
    }

    @Test
    void updateReward_successAndPartnerMissing() {
        UUID rewardId = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();
        Reward r = Reward.builder()
                .milestoneId(UUID.randomUUID())
                .rewardType(RewardType.VOUCHER)
                .name("R")
                .issuedCount(0)
                .active(true)
                .build();
        r.setId(rewardId);
        when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(r));
        when(partnerRepository.existsById(partnerId)).thenReturn(false);

        assertThrows(PartnerNotFoundException.class, () -> service.updateReward(
                new UpdateRewardCommand(rewardId, null, partnerId, null, null, null, null, null, null)));
    }

    @Test
    void updateReward_partialFields() {
        UUID rewardId = UUID.randomUUID();
        Reward r = Reward.builder()
                .milestoneId(UUID.randomUUID())
                .rewardType(RewardType.VOUCHER)
                .name("Old")
                .issuedCount(0)
                .active(false)
                .build();
        r.setId(rewardId);
        when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(r));
        when(rewardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var view = service.updateReward(new UpdateRewardCommand(
                rewardId, null, null, null, "New Name", "desc", BigDecimal.ONE, 14, 200));

        assertEquals("New Name", view.name());
    }

    @Test
    void activateReward_inactive_becomesActive() {
        UUID id = UUID.randomUUID();
        Reward r = Reward.builder()
                .milestoneId(UUID.randomUUID())
                .rewardType(RewardType.VOUCHER)
                .name("R")
                .issuedCount(0)
                .active(false)
                .build();
        r.setId(id);
        when(rewardRepository.findById(id)).thenReturn(Optional.of(r));
        when(rewardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertTrue(service.activateReward(id).active());
    }

    @Test
    void deactivatePartner_success() {
        UUID id = UUID.randomUUID();
        Partner p = Partner.builder().name("P").active(true).build();
        p.setId(id);
        when(partnerRepository.findById(id)).thenReturn(Optional.of(p));
        when(partnerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        assertFalse(service.deactivatePartner(id).active());
    }

    @Test
    void deprecatedMilestoneMethods_throwUnsupported() {
        assertThrows(UnsupportedOperationException.class, () -> service.createMilestone(null));
        assertThrows(UnsupportedOperationException.class, () -> service.updateMilestone(null));
        assertThrows(UnsupportedOperationException.class, () -> service.activateMilestone(UUID.randomUUID()));
        assertThrows(UnsupportedOperationException.class, () -> service.deactivateMilestone(UUID.randomUUID()));
    }

    @Test
    void bulkUploadVouchers_nonDuplicateIntegrityViolation_rethrows() {
        UUID rewardId = UUID.randomUUID();
        when(rewardRepository.existsById(rewardId)).thenReturn(true);
        DataIntegrityViolationException ex = new DataIntegrityViolationException("other constraint");
        when(voucherPoolRepository.saveAll(any())).thenThrow(ex);

        assertThrows(DataIntegrityViolationException.class, () ->
                service.bulkUploadVouchers(rewardId, List.of("CODE")));
    }

    @Test
    void activatePartner_notFound() {
        UUID id = UUID.randomUUID();
        when(partnerRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(PartnerNotFoundException.class, () -> service.activatePartner(id));
    }

    @Test
    void deactivatePartner_notFound() {
        UUID id = UUID.randomUUID();
        when(partnerRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(PartnerNotFoundException.class, () -> service.deactivatePartner(id));
    }

    @Test
    void createReward_withPartner_success() {
        UUID milestoneId = UUID.randomUUID();
        UUID partnerId = UUID.randomUUID();
        stubActiveMilestone(milestoneId);
        when(partnerRepository.existsById(partnerId)).thenReturn(true);
        when(rewardRepository.save(any())).thenAnswer(inv -> {
            Reward r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        var view = service.createReward(new CreateRewardCommand(
                milestoneId, partnerId, "VOUCHER", "Partner Voucher", null, null, 30, 100));

        assertEquals(partnerId, view.partnerId());
    }

    @Test
    void createReward_bonusStamp_supported() {
        UUID milestoneId = UUID.randomUUID();
        stubActiveMilestone(milestoneId);
        when(rewardRepository.save(any())).thenAnswer(inv -> {
            Reward r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        var view = service.createReward(new CreateRewardCommand(
                milestoneId, null, "BONUS_STAMP", "Bonus", null, null, null, null));

        assertEquals(RewardType.BONUS_STAMP, view.rewardType());
    }

    @Test
    void createReward_nullRewardType_rejected() {
        UUID milestoneId = UUID.randomUUID();
        stubActiveMilestone(milestoneId);

        assertThrows(InvalidMilestoneStateException.class, () -> service.createReward(
                new CreateRewardCommand(milestoneId, null, null, "Reward", null, null, 30, 100)));
    }

    @Test
    void updateReward_milestoneChange_success() {
        UUID rewardId = UUID.randomUUID();
        UUID newMilestoneId = UUID.randomUUID();
        Reward r = Reward.builder()
                .milestoneId(UUID.randomUUID())
                .rewardType(RewardType.VOUCHER)
                .name("R")
                .issuedCount(0)
                .active(true)
                .build();
        r.setId(rewardId);
        when(rewardRepository.findById(rewardId)).thenReturn(Optional.of(r));
        when(milestoneRepository.existsById(newMilestoneId)).thenReturn(true);
        when(rewardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var view = service.updateReward(new UpdateRewardCommand(
                rewardId, newMilestoneId, null, null, null, null, null, null, null));

        assertEquals(newMilestoneId, view.milestoneId());
    }

    @Test
    void updateReward_notFound() {
        UUID rewardId = UUID.randomUUID();
        when(rewardRepository.findById(rewardId)).thenReturn(Optional.empty());

        assertThrows(RewardNotFoundException.class, () -> service.updateReward(
                new UpdateRewardCommand(rewardId, null, null, null, null, null, null, null, null)));
    }

    @Test
    void activateReward_notFound() {
        UUID id = UUID.randomUUID();
        when(rewardRepository.findById(id)).thenReturn(Optional.empty());
        assertThrows(RewardNotFoundException.class, () -> service.activateReward(id));
    }

    @Test
    void updatePartner_allOptionalFields() {
        UUID id = UUID.randomUUID();
        Partner existing = Partner.builder().name("Old").active(true).build();
        existing.setId(id);
        when(partnerRepository.findById(id)).thenReturn(Optional.of(existing));
        when(partnerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var view = service.updatePartner(new UpdatePartnerCommand(
                id, "New", "https://logo", "https://banner", "mail@test.com",
                java.time.LocalDate.of(2026, 1, 1), java.time.LocalDate.of(2026, 12, 31)));

        assertEquals("New", view.name());
        assertEquals("https://logo", view.logoUrl());
        assertEquals("https://banner", view.bannerImageUrl());
    }

    @Test
    void bulkUploadVouchers_nullCodes_returnsZero() {
        UUID rewardId = UUID.randomUUID();
        when(rewardRepository.existsById(rewardId)).thenReturn(true);
        assertEquals(0, service.bulkUploadVouchers(rewardId, null));
        verify(voucherPoolRepository, never()).saveAll(any());
    }

    private void stubActiveMilestone(UUID milestoneId) {
        Milestone m = Milestone.builder()
                .campaignId(UUID.randomUUID())
                .code("M1")
                .stampsRequired(3)
                .name("M")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("T")
                .status(MilestoneStatus.ACTIVE)
                .sortOrder(0)
                .build();
        m.setId(milestoneId);
        when(milestoneRepository.findByIdNotDeleted(milestoneId)).thenReturn(Optional.of(m));
    }
}