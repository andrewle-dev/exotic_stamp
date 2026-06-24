package metro.ExoticStamp.modules.reward.domain.event;

import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RewardIssuedEventTest {

    @Test
    void milestoneIssued_hasUserRewardIdAndNullLegacyRewardId() {
        UUID userId = UUID.randomUUID();
        UUID userRewardId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();

        RewardIssuedEvent event = RewardIssuedEvent.milestoneIssued(
                userId, userRewardId, milestoneId, RewardType.VOUCHER);

        assertEquals(userId, event.getUserId());
        assertEquals(userRewardId, event.getUserRewardId());
        assertEquals(milestoneId, event.getMilestoneId());
        assertEquals(RewardType.VOUCHER, event.getRewardType());
        assertNull(event.getLegacyRewardId());
        assertNull(event.getRewardId());
        assertNull(event.getLineId());
    }
}
