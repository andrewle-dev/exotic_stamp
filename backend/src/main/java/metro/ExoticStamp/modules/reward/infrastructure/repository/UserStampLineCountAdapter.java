package metro.ExoticStamp.modules.reward.infrastructure.repository;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.reward.application.port.UserStampLineCountPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserStampLineCountAdapter implements UserStampLineCountPort {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public long countDistinctStationsOnLineForUserAndCampaign(UUID userId, UUID lineId, UUID campaignId) {
        Long c = jdbcTemplate.queryForObject(
                """
                        SELECT COUNT(DISTINCT us.station_id) FROM user_stamps us
                        INNER JOIN stations s ON s.id = us.station_id
                        WHERE us.user_id = ? AND s.line_id = ? AND us.campaign_id = ?
                        """,
                Long.class,
                userId,
                lineId,
                campaignId
        );
        return c != null ? c : 0L;
    }
}
