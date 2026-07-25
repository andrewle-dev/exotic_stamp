package metro.ExoticStamp.modules.reward.infrastructure;

import metro.ExoticStamp.modules.reward.application.port.RewardCachePort;
import metro.ExoticStamp.modules.reward.application.port.RewardReconcileLockPort;
import metro.ExoticStamp.modules.reward.application.service.RewardEvaluationService;
import metro.ExoticStamp.modules.reward.application.service.RewardIssuancePolicyService;
import metro.ExoticStamp.modules.reward.application.service.RewardReconcileService;
import metro.ExoticStamp.modules.reward.application.service.VoucherAllocationService;
import metro.ExoticStamp.modules.reward.application.support.RewardAuditHelper;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.service.MilestoneDomainService;
import metro.ExoticStamp.modules.reward.infrastructure.repository.JpaMilestoneRepository;
import metro.ExoticStamp.modules.reward.infrastructure.repository.JpaPartnerRepository;
import metro.ExoticStamp.modules.reward.infrastructure.repository.JpaRewardRepository;
import metro.ExoticStamp.modules.reward.infrastructure.repository.JpaUserRewardRepository;
import metro.ExoticStamp.modules.reward.infrastructure.repository.JpaVoucherPoolRepository;
import metro.ExoticStamp.modules.reward.infrastructure.repository.MilestoneRepositoryAdapter;
import metro.ExoticStamp.modules.reward.infrastructure.repository.PartnerRepositoryAdapter;
import metro.ExoticStamp.modules.reward.infrastructure.repository.RewardReconcileCandidateAdapter;
import metro.ExoticStamp.modules.reward.infrastructure.repository.RewardReconcileAdvisoryLockAdapter;
import metro.ExoticStamp.modules.reward.infrastructure.repository.RewardRepositoryAdapter;
import metro.ExoticStamp.modules.reward.infrastructure.repository.UserRewardRepositoryAdapter;
import metro.ExoticStamp.modules.reward.infrastructure.repository.UserStampCampaignCountAdapter;
import metro.ExoticStamp.modules.reward.infrastructure.repository.VoucherPoolRepositoryAdapter;
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
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Proves Option B recovery: stamps committed without a reward are repaired by reconcile,
 * and a second reconcile is idempotent via {@code uq_user_rewards_once}.
 */
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
        RewardReconcileCandidateAdapter.class,
        RewardReconcileAdvisoryLockAdapter.class,
        RewardReconcileService.class,
        RewardReconcileIT.TestConfig.class
})
class RewardReconcileIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void registerPg(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> {
            String url = postgres.getJdbcUrl();
            String sep = url.contains("?") ? "&" : "?";
            return url + sep + "stringtype=unspecified";
        });
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired private RewardReconcileService rewardReconcileService;

    @MockBean private RewardCachePort rewardCachePort;
    @MockBean private RewardAuditHelper rewardAuditHelper;
    @MockBean private ApplicationEventPublisher applicationEventPublisher;

    private UUID userId;
    private UUID campaignId;
    private UUID milestoneId;

    @BeforeEach
    void seed() {
        UUID lineId = UUID.randomUUID();
        UUID stationId = UUID.randomUUID();
        UUID designId = UUID.randomUUID();
        campaignId = UUID.randomUUID();
        milestoneId = UUID.randomUUID();
        userId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.update(
                """
                INSERT INTO users (id, username, email, phone_number, password, status, token_version, created_at)
                VALUES (?,?,?,?,?,?,?,?)
                """,
                userId, "u-" + userId.toString().substring(0, 8),
                "u-" + userId.toString().substring(0, 8) + "@example.com",
                "+1555" + userId.toString().replace("-", "").substring(0, 7),
                "hashed", "ACTIVE", 0L, now);

        jdbcTemplate.update(
                "INSERT INTO lines (id, code, name, display_name, total_stations, status, sort_order) VALUES (?,?,?,?,?,?,?)",
                lineId, "LR", "Line", "Line", 1, "ACTIVE", 0);
        jdbcTemplate.update(
                "INSERT INTO stations (id, line_id, code, name, display_name, sort_order, status, collector_count) VALUES (?,?,?,?,?,?,?,?)",
                stationId, lineId, "S1", "S1", "S1", 1, "ACTIVE", 0);
        jdbcTemplate.update(
                """
                INSERT INTO campaigns (
                    id, code, name, display_name, description, campaign_type, status,
                    start_at, end_at, priority, line_id, is_default
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                """,
                campaignId, "CMP-R", "Camp", "Camp", "d", "STANDARD", "ACTIVE",
                now, now.plusYears(1), 0, lineId, true);
        jdbcTemplate.update(
                """
                INSERT INTO stamp_designs (
                    id, station_id, campaign_id, name, image_url, rarity, status, sort_order, is_limited
                ) VALUES (?,?,?,?,?,?,?,?,?)
                """,
                designId, stationId, campaignId, "D1", "https://x/1.png", "COMMON", "ACTIVE", 0, false);
        jdbcTemplate.update(
                """
                INSERT INTO milestones (id, line_id, campaign_id, code, stamps_required, name, description,
                reward_type, reward_title, status, sort_order, is_active)
                VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                """,
                milestoneId, lineId, campaignId, "M1", 1, "M1", "one stamp",
                RewardType.DIGITAL_STICKER.name(), "Prize", "ACTIVE", 0, true);

        // Stamp committed without reward — simulates missed AFTER_COMMIT listener.
        jdbcTemplate.update(
                """
                INSERT INTO user_stamps (id, user_id, station_id, stamp_design_id, campaign_id, collected_at,
                gps_verified, collect_method, device_fingerprint, idempotency_key)
                VALUES (?,?,?,?,?,?,?,?::collect_method_enum,?,?)
                """,
                UUID.randomUUID(), userId, stationId, designId, campaignId, now,
                false, "NFC", "device", "idem-" + userId);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void missedListener_reconcileIssuesOnce_andSecondPassIsIdempotent() {
        Long before = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_rewards WHERE user_id = ? AND milestone_id = ?",
                Long.class, userId, milestoneId);
        assertEquals(0L, before);

        var first = rewardReconcileService.runReconcile();
        assertFalse(first.skipped());
        assertEquals(1, first.missingExamined());
        assertEquals(1, first.missingRepaired());

        Long afterFirst = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_rewards WHERE user_id = ? AND milestone_id = ?",
                Long.class, userId, milestoneId);
        assertEquals(1L, afterFirst);

        var second = rewardReconcileService.runReconcile();
        assertEquals(0, second.missingExamined(), "Candidate query must exclude already-rewarded milestones");

        Long afterSecond = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_rewards WHERE user_id = ? AND milestone_id = ?",
                Long.class, userId, milestoneId);
        assertEquals(1L, afterSecond);
    }

    @Configuration
    static class TestConfig {
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

        @Bean
        RewardProperties rewardProperties() {
            RewardProperties props = new RewardProperties();
            props.setReconcileLookback(Duration.ofHours(48));
            props.setReconcileBatchSize(50);
            props.setReconcileMaxDuration(Duration.ofSeconds(30));
            props.setReconcileMaxBatches(3);
            props.setReconcileMaxBatchSize(100);
            return props;
        }
    }
}
