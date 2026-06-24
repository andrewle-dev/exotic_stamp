package metro.ExoticStamp.modules.reward.infrastructure;

import metro.ExoticStamp.modules.reward.application.port.RewardCachePort;
import metro.ExoticStamp.modules.reward.application.service.RewardCommandService;
import metro.ExoticStamp.modules.reward.application.service.RewardEvaluationService;
import metro.ExoticStamp.modules.reward.application.service.RewardIssuancePolicyService;
import metro.ExoticStamp.modules.reward.application.service.VoucherAllocationService;
import metro.ExoticStamp.modules.reward.application.support.RewardAuditHelper;
import metro.ExoticStamp.modules.reward.domain.event.RewardIssuedEvent;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.infrastructure.repository.JpaMilestoneRepository;
import metro.ExoticStamp.modules.reward.infrastructure.repository.JpaPartnerRepository;
import metro.ExoticStamp.modules.reward.infrastructure.repository.JpaRewardRepository;
import metro.ExoticStamp.modules.reward.infrastructure.repository.JpaUserRewardRepository;
import metro.ExoticStamp.modules.reward.infrastructure.repository.JpaVoucherPoolRepository;
import metro.ExoticStamp.modules.reward.infrastructure.repository.MilestoneRepositoryAdapter;
import metro.ExoticStamp.modules.reward.infrastructure.repository.PartnerRepositoryAdapter;
import metro.ExoticStamp.modules.reward.infrastructure.repository.RewardRepositoryAdapter;
import metro.ExoticStamp.modules.reward.infrastructure.repository.UserRewardRepositoryAdapter;
import metro.ExoticStamp.modules.reward.infrastructure.repository.UserStampCampaignCountAdapter;
import metro.ExoticStamp.modules.reward.infrastructure.repository.VoucherPoolRepositoryAdapter;
import metro.ExoticStamp.modules.reward.domain.service.MilestoneDomainService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.transaction.TestTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = "metro.ExoticStamp.modules.reward.domain.model")
@org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackageClasses = {
        JpaPartnerRepository.class,
        JpaMilestoneRepository.class,
        JpaRewardRepository.class,
        JpaUserRewardRepository.class,
        JpaVoucherPoolRepository.class
})
@Import({
        PartnerRepositoryAdapter.class,
        MilestoneRepositoryAdapter.class,
        RewardRepositoryAdapter.class,
        UserRewardRepositoryAdapter.class,
        VoucherPoolRepositoryAdapter.class,
        UserStampCampaignCountAdapter.class,
        RewardIssuancePolicyService.class,
        VoucherAllocationService.class,
        RewardEvaluationService.class,
        RewardCommandService.class,
        RewardStampCollectedFlowIT.TestClockConfig.class
})
class RewardStampCollectedFlowIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void registerPg(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RewardCommandService rewardCommandService;

    @MockBean
    private RewardCachePort rewardCachePort;

    @MockBean
    private RewardAuditHelper rewardAuditHelper;

    @MockBean
    private ApplicationEventPublisher applicationEventPublisher;

    private UUID lineId;
    private UUID stationId1;
    private UUID stationId2;
    private UUID stationId3;
    private UUID campaignId;
    private UUID stampDesignId1;
    private UUID stampDesignId2;
    private UUID stampDesignId3;
    private UUID userId;
    private UUID milestoneId;

    @BeforeEach
    void seed() {
        lineId = UUID.randomUUID();
        stationId1 = UUID.randomUUID();
        stationId2 = UUID.randomUUID();
        stationId3 = UUID.randomUUID();
        campaignId = UUID.randomUUID();
        stampDesignId1 = UUID.randomUUID();
        stampDesignId2 = UUID.randomUUID();
        stampDesignId3 = UUID.randomUUID();
        userId = UUID.randomUUID();
        milestoneId = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(
                "INSERT INTO lines (id, code, name, total_stations, is_active) VALUES (?,?,?,?,?)",
                lineId, "L" + lineId.toString().substring(0, 4), "Test Line", 3, true);

        jdbcTemplate.update(
                "INSERT INTO stations (id, line_id, code, name, sequence, is_active, collector_count) VALUES (?,?,?,?,?,?,?)",
                stationId1, lineId, "S1", "Station 1", 1, true, 0);
        jdbcTemplate.update(
                "INSERT INTO stations (id, line_id, code, name, sequence, is_active, collector_count) VALUES (?,?,?,?,?,?,?)",
                stationId2, lineId, "S2", "Station 2", 2, true, 0);
        jdbcTemplate.update(
                "INSERT INTO stations (id, line_id, code, name, sequence, is_active, collector_count) VALUES (?,?,?,?,?,?,?)",
                stationId3, lineId, "S3", "Station 3", 3, true, 0);

        jdbcTemplate.update(
                "INSERT INTO campaigns (id, code, name, description, start_date, end_date, is_active, line_id, is_default) VALUES (?,?,?,?,?,?,?,?,?)",
                campaignId, "CMP-" + campaignId.toString().substring(0, 8), "Camp", "d",
                now, now.plusYears(1), true, lineId, true);

        jdbcTemplate.update(
                "INSERT INTO stamp_designs (id, station_id, campaign_id, name, artwork_url, is_limited, is_active) VALUES (?,?,?,?,?,?,?)",
                stampDesignId1, stationId1, campaignId, "D1", "https://example.com/1.png", false, true);
        jdbcTemplate.update(
                "INSERT INTO stamp_designs (id, station_id, campaign_id, name, artwork_url, is_limited, is_active) VALUES (?,?,?,?,?,?,?)",
                stampDesignId2, stationId2, campaignId, "D2", "https://example.com/2.png", false, true);
        jdbcTemplate.update(
                "INSERT INTO stamp_designs (id, station_id, campaign_id, name, artwork_url, is_limited, is_active) VALUES (?,?,?,?,?,?,?)",
                stampDesignId3, stationId3, campaignId, "D3", "https://example.com/3.png", false, true);

        jdbcTemplate.update(
                """
                        INSERT INTO milestones (id, line_id, campaign_id, code, stamps_required, name, description,
                        reward_type, reward_title, status, sort_order, is_active)
                        VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                        """,
                milestoneId, lineId, campaignId, "M3", 3, "M3", "three stamps",
                RewardType.DIGITAL_STICKER.name(), "Prize", "ACTIVE", 0, true);

        insertUserStamp(stationId1, stampDesignId1, "fp1", now);
        insertUserStamp(stationId2, stampDesignId2, "fp2", now);
        insertUserStamp(stationId3, stampDesignId3, "fp3", now);
    }

    private void insertUserStamp(UUID stationId, UUID designId, String fp, LocalDateTime collectedAt) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_stamps (id, user_id, station_id, stamp_design_id, campaign_id, collected_at,
                        gps_verified, collect_method, device_fingerprint, idempotency_key)
                        VALUES (?,?,?,?,?,?,?,?::collect_method_enum,?,?)
                        """,
                UUID.randomUUID(),
                userId,
                stationId,
                designId,
                campaignId,
                collectedAt,
                false,
                "NFC",
                "device",
                fp
        );
    }

    @Test
    @Transactional
    void handleStampCollected_persistsUserReward_andPublishesAfterCommit() {
        rewardCommandService.handleStampCollected(userId, lineId, campaignId);
        Long cnt = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_rewards WHERE user_id = ? AND milestone_id = ?",
                Long.class,
                userId,
                milestoneId
        );
        assertEquals(1L, cnt);
        TestTransaction.flagForCommit();
        TestTransaction.end();
        TestTransaction.start();
        verify(applicationEventPublisher).publishEvent(any(RewardIssuedEvent.class));
    }

    @Configuration
    static class TestClockConfig {
        @Bean
        Clock clock() {
            return Clock.fixed(Instant.parse("2026-04-12T15:00:00Z"), ZoneOffset.UTC);
        }

        @Bean
        io.micrometer.core.instrument.MeterRegistry meterRegistry() {
            return new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        }

        @Bean
        MilestoneDomainService milestoneDomainService() {
            return new MilestoneDomainService();
        }
    }
}
