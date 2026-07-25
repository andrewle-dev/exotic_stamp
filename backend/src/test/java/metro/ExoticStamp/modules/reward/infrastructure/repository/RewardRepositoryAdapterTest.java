package metro.ExoticStamp.modules.reward.infrastructure.repository;

import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.model.Partner;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RewardRepositoryAdapterTest {

    @Mock private JpaMilestoneRepository jpaMilestoneRepository;
    @Mock private JpaVoucherPoolRepository jpaVoucherPoolRepository;
    @Mock private JpaPartnerRepository jpaPartnerRepository;

    @InjectMocks private MilestoneRepositoryAdapter milestoneAdapter;
    @InjectMocks private VoucherPoolRepositoryAdapter voucherPoolAdapter;
    @InjectMocks private PartnerRepositoryAdapter partnerAdapter;

    @Test
    void milestoneAdapter_findByIdNotDeleted_delegates() {
        UUID id = UUID.randomUUID();
        Milestone m = sampleMilestone();
        when(jpaMilestoneRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(m));

        assertTrue(milestoneAdapter.findByIdNotDeleted(id).isPresent());
        verify(jpaMilestoneRepository).findByIdAndDeletedAtIsNull(id);
    }

    @Test
    void milestoneAdapter_findAllNotDeletedPaged_mapsSlice() {
        UUID campaignId = UUID.randomUUID();
        Milestone m = sampleMilestone();
        Page<Milestone> page = new PageImpl<>(List.of(m), PageRequest.of(0, 20), 1);
        when(jpaMilestoneRepository.findAllNotDeleted(eq(campaignId), eq(MilestoneStatus.ACTIVE), any()))
                .thenReturn(page);

        PagedSlice<Milestone> slice = milestoneAdapter.findAllNotDeletedPaged(
                campaignId, MilestoneStatus.ACTIVE, 0, 20);

        assertEquals(1, slice.content().size());
        assertEquals(1, slice.totalElements());
    }

    @Test
    void milestoneAdapter_legacyFindAllPaged_activeOnly() {
        Milestone m = sampleMilestone();
        Page<Milestone> page = new PageImpl<>(List.of(m), PageRequest.of(0, 10), 1);
        when(jpaMilestoneRepository.findAllNotDeleted(eq(null), eq(MilestoneStatus.ACTIVE), any()))
                .thenReturn(page);

        PagedSlice<Milestone> slice = milestoneAdapter.findAllPaged(true, 0, 10);

        assertEquals(1, slice.content().size());
    }

    @Test
    void voucherPoolAdapter_findByMilestoneIdPaged_delegates() {
        UUID milestoneId = UUID.randomUUID();
        VoucherPool vp = VoucherPool.builder()
                .milestoneId(milestoneId)
                .code("C")
                .status(VoucherPoolStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .build();
        Page<VoucherPool> page = new PageImpl<>(List.of(vp), PageRequest.of(0, 20), 1);
        when(jpaVoucherPoolRepository.findFiltered(eq(milestoneId), eq(VoucherPoolStatus.AVAILABLE), any()))
                .thenReturn(page);

        var slice = voucherPoolAdapter.findByMilestoneIdPaged(milestoneId, VoucherPoolStatus.AVAILABLE, 0, 20);

        assertEquals(1, slice.content().size());
    }

    @Test
    void partnerAdapter_findAllPaged_activeFilter() {
        Partner p = Partner.builder().name("P").active(true).build();
        Page<Partner> page = new PageImpl<>(List.of(p), PageRequest.of(0, 20), 1);
        when(jpaPartnerRepository.findByActive(true, PageRequest.of(0, 20))).thenReturn(page);

        var slice = partnerAdapter.findAllPaged(true, 0, 20);

        assertEquals(1, slice.content().size());
        verify(jpaPartnerRepository).findByActive(true, PageRequest.of(0, 20));
    }

    @Test
    void partnerAdapter_findAllPaged_noFilter() {
        Page<Partner> page = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);
        when(jpaPartnerRepository.findAll(PageRequest.of(0, 20))).thenReturn(page);

        var slice = partnerAdapter.findAllPaged(null, 0, 20);

        assertTrue(slice.content().isEmpty());
        verify(jpaPartnerRepository).findAll(PageRequest.of(0, 20));
    }

    private static Milestone sampleMilestone() {
        return Milestone.builder()
                .campaignId(UUID.randomUUID())
                .code("M1")
                .stampsRequired(3)
                .name("M")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("T")
                .status(MilestoneStatus.ACTIVE)
                .sortOrder(0)
                .build();
    }
}
