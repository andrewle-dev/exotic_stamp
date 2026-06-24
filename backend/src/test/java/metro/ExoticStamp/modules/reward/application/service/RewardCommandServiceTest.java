package metro.ExoticStamp.modules.reward.application.service;

import metro.ExoticStamp.modules.reward.domain.exception.RedeemNotSupportedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RewardCommandServiceTest {

    @Mock
    private RewardEvaluationService rewardEvaluationService;

    private RewardCommandService service;

    @BeforeEach
    void setUp() {
        service = new RewardCommandService(rewardEvaluationService);
    }

    @Test
    void handleStampCollected_delegatesToEvaluationService() {
        UUID userId = UUID.randomUUID();
        UUID lineId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        service.handleStampCollected(userId, lineId, campaignId);
        verify(rewardEvaluationService).handleStampCollected(userId, campaignId);
    }

    @Test
    void redeemVoucher_disabledInMvp() {
        UUID userId = UUID.randomUUID();
        UUID userRewardId = UUID.randomUUID();
        assertThrows(RedeemNotSupportedException.class, () -> service.redeemVoucher(userId, userRewardId));
    }
}
