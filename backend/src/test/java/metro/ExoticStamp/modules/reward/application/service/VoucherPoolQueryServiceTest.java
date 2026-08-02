package metro.ExoticStamp.modules.reward.application.service;

import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.application.view.VoucherPoolView;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import metro.ExoticStamp.modules.reward.domain.exception.InvalidMilestoneStateException;
import metro.ExoticStamp.modules.reward.domain.model.PagedSlice;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPool;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;
import metro.ExoticStamp.modules.reward.domain.repository.VoucherPoolRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VoucherPoolQueryServiceTest {

    @Mock private VoucherPoolRepository voucherPoolRepository;

    private VoucherPoolQueryService service;
    private final UUID milestoneId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        RewardProperties props = new RewardProperties();
        props.setDefaultPageSize(20);
        props.setMaxPageSize(50);
        service = new VoucherPoolQueryService(voucherPoolRepository, new RewardAppMapper(), props);
    }

    @Test
    void list_normalizesPageAndSize() {
        VoucherPool vp = sampleVoucher();
        when(voucherPoolRepository.findByMilestoneIdPaged(
                eq(milestoneId), eq(VoucherPoolStatus.AVAILABLE), eq(0), eq(50)))
                .thenReturn(new PagedSlice<>(List.of(vp), 1, 1, 0, 50));

        PageResponse<VoucherPoolView> page = service.list(milestoneId, "AVAILABLE", -1, 999);

        assertEquals(1, page.content().size());
        assertEquals("CODE1", page.content().get(0).code());
    }

    @Test
    void list_nullStatus_queriesAll() {
        when(voucherPoolRepository.findByMilestoneIdPaged(
                eq(milestoneId), isNull(), eq(0), eq(20)))
                .thenReturn(new PagedSlice<>(List.of(), 0, 0, 0, 20));

        PageResponse<VoucherPoolView> page = service.list(milestoneId, null, 0, 0);

        assertEquals(0, page.content().size());
        verify(voucherPoolRepository).findByMilestoneIdPaged(milestoneId, null, 0, 20);
    }

    @Test
    void get_found() {
        UUID id = UUID.randomUUID();
        VoucherPool vp = sampleVoucher();
        vp.setId(id);
        when(voucherPoolRepository.findById(id)).thenReturn(Optional.of(vp));

        VoucherPoolView view = service.get(id);

        assertEquals(id, view.id());
        assertEquals(VoucherPoolStatus.AVAILABLE, view.status());
    }

    @Test
    void get_notFound() {
        UUID id = UUID.randomUUID();
        when(voucherPoolRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(InvalidMilestoneStateException.class, () -> service.get(id));
    }

    private VoucherPool sampleVoucher() {
        return VoucherPool.builder()
                .milestoneId(milestoneId)
                .code("CODE1")
                .status(VoucherPoolStatus.AVAILABLE)
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .build();
    }
}
