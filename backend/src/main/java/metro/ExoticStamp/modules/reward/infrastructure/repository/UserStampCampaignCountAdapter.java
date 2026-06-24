package metro.ExoticStamp.modules.reward.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.reward.application.port.UserStampCampaignCountPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserStampCampaignCountAdapter implements UserStampCampaignCountPort {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public long countDistinctStationsByUserIdAndCampaignId(UUID userId, UUID campaignId) {
        Long count = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(DISTINCT station_id) FROM user_stamps
                        WHERE user_id = ? AND campaign_id = ?
                        """,
                Long.class,
                userId,
                campaignId
        );
        return count != null ? count : 0L;
    }
}
