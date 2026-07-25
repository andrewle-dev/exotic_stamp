package metro.ExoticStamp.modules.reward.application.service;

import metro.ExoticStamp.modules.reward.application.command.ImportVouchersCommand;
import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.application.support.RewardAuditHelper;
import metro.ExoticStamp.modules.reward.domain.exception.InvalidMilestoneStateException;
import metro.ExoticStamp.modules.reward.domain.exception.MilestoneNotFoundException;
import metro.ExoticStamp.modules.reward.domain.exception.VoucherCodeDuplicateException;
import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;
import metro.ExoticStamp.modules.reward.domain.repository.MilestoneRepository;
import metro.ExoticStamp.modules.reward.domain.repository.VoucherPoolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherPoolCommandServiceTest {

    @Mock private MilestoneRepository milestoneRepository;
    @Mock private VoucherPoolRepository voucherPoolRepository;
    @Mock private RewardAuditHelper rewardAuditHelper;

    private VoucherPoolCommandService service;
    private final UUID milestoneId = UUID.randomUUID();
    private final Clock clock = Clock.fixed(Instant.parse("2026-06-15T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new VoucherPoolCommandService(
                milestoneRepository, voucherPoolRepository, new RewardAppMapper(), rewardAuditHelper, clock);
    }

    @Test
    void importVouchers_nonVoucherMilestone_rejected() {
        stubMilestone(RewardType.DIGITAL_STICKER);

        assertThrows(InvalidMilestoneStateException.class, () -> service.importVouchers(
                new ImportVouchersCommand(milestoneId, List.of("A"), null)));
    }

    @Test
    void importVouchers_milestoneNotFound() {
        when(milestoneRepository.findByIdNotDeleted(milestoneId)).thenReturn(Optional.empty());

        assertThrows(MilestoneNotFoundException.class, () -> service.importVouchers(
                new ImportVouchersCommand(milestoneId, List.of("A"), null)));
    }

    @Test
    void importVouchers_duplicateInBatch_rejected() {
        stubMilestone(RewardType.VOUCHER);

        assertThrows(VoucherCodeDuplicateException.class, () -> service.importVouchers(
                new ImportVouchersCommand(milestoneId, List.of("SAME", "SAME"), null)));
    }

    @Test
    void importVouchers_skipsBlankAndTrims() {
        stubMilestone(RewardType.VOUCHER);
        when(voucherPoolRepository.saveAll(any())).thenAnswer(inv -> (List<VoucherPool>) inv.getArgument(0));

        int count = service.importVouchers(new ImportVouchersCommand(
                milestoneId, Arrays.asList("  CODE1  ", "", null, "CODE2"),
                LocalDateTime.of(2026, 12, 31, 23, 59)));

        assertEquals(2, count);
        verify(rewardAuditHelper).scheduleVoucherImported(milestoneId, 2);
    }

    @Test
    void importVouchers_emptyCodes_returnsZero() {
        stubMilestone(RewardType.VOUCHER);

        assertEquals(0, service.importVouchers(new ImportVouchersCommand(milestoneId, List.of(), null)));
        verify(voucherPoolRepository, never()).saveAll(any());
    }

    @Test
    void importVouchers_dbDuplicate_mapsException() {
        stubMilestone(RewardType.VOUCHER);
        when(voucherPoolRepository.saveAll(any())).thenThrow(
                new DataIntegrityViolationException("dup", new RuntimeException("uq_voucher_pool_code")));

        assertThrows(VoucherCodeDuplicateException.class, () -> service.importVouchers(
                new ImportVouchersCommand(milestoneId, List.of("X"), null)));
    }

    @Test
    void disable_claimedVoucher_rejected() {
        UUID id = UUID.randomUUID();
        VoucherPool vp = VoucherPool.builder()
                .milestoneId(milestoneId)
                .code("C")
                .status(VoucherPoolStatus.CLAIMED)
                .build();
        vp.setId(id);
        when(voucherPoolRepository.findById(id)).thenReturn(Optional.of(vp));

        assertThrows(InvalidMilestoneStateException.class, () -> service.disable(id));
    }

    @Test
    void disable_available_setsDisabled() {
        UUID id = UUID.randomUUID();
        VoucherPool vp = VoucherPool.builder()
                .milestoneId(milestoneId)
                .code("C")
                .status(VoucherPoolStatus.AVAILABLE)
                .build();
        vp.setId(id);
        when(voucherPoolRepository.findById(id)).thenReturn(Optional.of(vp));
        when(voucherPoolRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var view = service.disable(id);

        assertEquals(VoucherPoolStatus.DISABLED, view.status());
        verify(rewardAuditHelper).scheduleVoucherDisabled(id);
    }

    private void stubMilestone(RewardType type) {
        Milestone m = Milestone.builder()
                .campaignId(UUID.randomUUID())
                .code("M1")
                .stampsRequired(3)
                .name("M")
                .rewardType(type)
                .rewardTitle("T")
                .status(MilestoneStatus.ACTIVE)
                .sortOrder(0)
                .build();
        when(milestoneRepository.findByIdNotDeleted(milestoneId)).thenReturn(Optional.of(m));
    }
}
