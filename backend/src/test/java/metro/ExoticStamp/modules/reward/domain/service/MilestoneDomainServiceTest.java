package metro.ExoticStamp.modules.reward.domain.service;

import metro.ExoticStamp.modules.reward.domain.model.Milestone;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MilestoneDomainServiceTest {

    private final MilestoneDomainService service = new MilestoneDomainService();

    @Test
    void returnsEmptyWhenNoMilestones() {
        assertTrue(service.findNewlyCompletedMilestones(5, List.of(), Set.of()).isEmpty());
    }

    @Test
    void ordersByStampsRequiredAndSkipsRewarded() {
        UUID m1 = UUID.randomUUID();
        UUID m2 = UUID.randomUUID();
        UUID m3 = UUID.randomUUID();
        List<Milestone> milestones = List.of(
                milestone(m3, 10),
                milestone(m1, 3),
                milestone(m2, 5)
        );
        List<Milestone> out = service.findNewlyCompletedMilestones(10, milestones, Set.of(m1));
        assertEquals(2, out.size());
        assertEquals(5, out.get(0).getStampsRequired());
        assertEquals(10, out.get(1).getStampsRequired());
    }

    @Test
    void exactBoundaryIncluded() {
        UUID m = UUID.randomUUID();
        List<Milestone> out = service.findNewlyCompletedMilestones(5, List.of(milestone(m, 5)), Set.of());
        assertEquals(1, out.size());
    }

    @Test
    void belowThresholdExcluded() {
        UUID m = UUID.randomUUID();
        List<Milestone> out = service.findNewlyCompletedMilestones(4, List.of(milestone(m, 5)), Set.of());
        assertTrue(out.isEmpty());
    }

    private static Milestone milestone(UUID id, int stamps) {
        return Milestone.builder()
                .id(id)
                .stampsRequired(stamps)
                .name("M")
                .active(true)
                .build();
    }
}
