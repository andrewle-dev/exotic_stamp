package metro.ExoticStamp.modules.reward.infrastructure;

import metro.ExoticStamp.modules.reward.application.port.RewardCachePort;
import metro.ExoticStamp.modules.reward.application.service.RewardEvaluationService;
import metro.ExoticStamp.modules.reward.application.service.RewardIssuancePolicyService;
import metro.ExoticStamp.modules.reward.application.service.VoucherAllocationService;
import metro.ExoticStamp.modules.reward.application.service.VoucherPoolCommandService;
import metro.ExoticStamp.modules.reward.application.support.RewardAuditHelper;
import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.application.command.ImportVouchersCommand;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;
import metro.ExoticStamp.modules.reward.domain.service.MilestoneDomainService;
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
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * DB-backed concurrency proofs for Stage 5 reward engine (Docker-gated).
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
        VoucherPoolCommandService.class,
        RewardAppMapper.class,
        RewardEvaluationService.class,
        RewardConcurrencyIT.TestClockConfig.class
})
class RewardConcurrencyIT {

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
    @Autowired private RewardEvaluationService rewardEvaluationService;
    @Autowired private VoucherPoolCommandService voucherPoolCommandService;
    @Autowired private PlatformTransactionManager transactionManager;

    @MockBean private RewardCachePort rewardCachePort;
    @MockBean private RewardAuditHelper rewardAuditHelper;
    @MockBean private ApplicationEventPublisher applicationEventPublisher;

    private UUID lineId;
    private UUID campaignId;
    private UUID milestoneId;
    private UUID userA;
    private UUID userB;

    @BeforeEach
    void seedCommitted() {
        lineId = UUID.randomUUID();
        campaignId = UUID.randomUUID();
        milestoneId = UUID.randomUUID();
        userA = UUID.randomUUID();
        userB = UUID.randomUUID();

        TransactionTemplate commit = new TransactionTemplate(transactionManager);
        commit.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        commit.executeWithoutResult(status -> insertBaseCampaignAndMilestone());
    }

    private void insertBaseCampaignAndMilestone() {
        LocalDateTime now = LocalDateTime.now();
        for (UUID userId : List.of(userA, userB)) {
            jdbcTemplate.update(
                    """
                    INSERT INTO users (id, username, email, phone_number, password, status, token_version, created_at)
                    VALUES (?,?,?,?,?,?,?,?)
                    """,
                    userId,
                    "u-" + userId.toString().substring(0, 8),
                    "u-" + userId.toString().substring(0, 8) + "@example.com",
                    "+1555" + userId.toString().replace("-", "").substring(0, 7),
                    "hashed-password-not-used",
                    "ACTIVE",
                    0L,
                    now);
        }

        jdbcTemplate.update(
                """
                INSERT INTO lines (id, code, name, display_name, total_stations, status, sort_order)
                VALUES (?,?,?,?,?,?,?)
                """,
                lineId, "L" + lineId.toString().substring(0, 8), "Line", "Line", 3, "ACTIVE", 0);

        UUID s1 = UUID.randomUUID();
        UUID s2 = UUID.randomUUID();
        UUID s3 = UUID.randomUUID();
        jdbcTemplate.update(
                """
                INSERT INTO stations (id, line_id, code, name, display_name, sort_order, status, collector_count)
                VALUES (?,?,?,?,?,?,?,?)
                """,
                s1, lineId, "S1", "S1", "S1", 1, "ACTIVE", 0);
        jdbcTemplate.update(
                """
                INSERT INTO stations (id, line_id, code, name, display_name, sort_order, status, collector_count)
                VALUES (?,?,?,?,?,?,?,?)
                """,
                s2, lineId, "S2", "S2", "S2", 2, "ACTIVE", 0);
        jdbcTemplate.update(
                """
                INSERT INTO stations (id, line_id, code, name, display_name, sort_order, status, collector_count)
                VALUES (?,?,?,?,?,?,?,?)
                """,
                s3, lineId, "S3", "S3", "S3", 3, "ACTIVE", 0);

        jdbcTemplate.update(
                """
                INSERT INTO campaigns (
                    id, code, name, display_name, description, campaign_type, status,
                    start_at, end_at, priority, line_id, is_default
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                """,
                campaignId, "CMP-" + campaignId.toString().substring(0, 8), "Camp", "Camp", "d",
                "STANDARD", "ACTIVE", now, now.plusYears(1), 0, lineId, true);

        UUID d1 = UUID.randomUUID();
        UUID d2 = UUID.randomUUID();
        UUID d3 = UUID.randomUUID();
        jdbcTemplate.update(
                """
                INSERT INTO stamp_designs (
                    id, station_id, campaign_id, name, image_url, rarity, status, sort_order, is_limited
                ) VALUES (?,?,?,?,?,?,?,?,?)
                """,
                d1, s1, campaignId, "D1", "https://x/1.png", "COMMON", "ACTIVE", 0, false);
        jdbcTemplate.update(
                """
                INSERT INTO stamp_designs (
                    id, station_id, campaign_id, name, image_url, rarity, status, sort_order, is_limited
                ) VALUES (?,?,?,?,?,?,?,?,?)
                """,
                d2, s2, campaignId, "D2", "https://x/2.png", "COMMON", "ACTIVE", 0, false);
        jdbcTemplate.update(
                """
                INSERT INTO stamp_designs (
                    id, station_id, campaign_id, name, image_url, rarity, status, sort_order, is_limited
                ) VALUES (?,?,?,?,?,?,?,?,?)
                """,
                d3, s3, campaignId, "D3", "https://x/3.png", "COMMON", "ACTIVE", 0, false);

        jdbcTemplate.update(
                """
                        INSERT INTO milestones (id, line_id, campaign_id, code, stamps_required, name, description,
                        reward_type, reward_title, status, sort_order, is_active)
                        VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                        """,
                milestoneId, lineId, campaignId, "VM-" + milestoneId.toString().substring(0, 8), 1, "Voucher M", "desc",
                RewardType.VOUCHER.name(), "Voucher Prize", "ACTIVE", 0, true);

        for (UUID userId : List.of(userA, userB)) {
            insertStamp(userId, s1, d1, "fp-" + userId + "-1");
        }
    }

    private void insertStamp(UUID userId, UUID stationId, UUID designId, String fp) {
        jdbcTemplate.update(
                """
                        INSERT INTO user_stamps (id, user_id, station_id, stamp_design_id, campaign_id, collected_at,
                        gps_verified, collect_method, device_fingerprint, idempotency_key)
                        VALUES (?,?,?,?,?,?,?,?::collect_method_enum,?,?)
                        """,
                UUID.randomUUID(), userId, stationId, designId, campaignId, LocalDateTime.now(),
                false, "NFC", "device", fp
        );
    }

    private void insertSingleAvailableVoucher() {
        TransactionTemplate commit = new TransactionTemplate(transactionManager);
        commit.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        commit.executeWithoutResult(status -> jdbcTemplate.update(
                """
                        INSERT INTO voucher_pool (id, milestone_id, code, status, is_redeemed, created_at)
                        VALUES (?,?,?,?,?,?)
                        """,
                UUID.randomUUID(), milestoneId, "ONLY-VOUCHER-" + UUID.randomUUID().toString().substring(0, 8),
                VoucherPoolStatus.AVAILABLE.name(), false, LocalDateTime.now()
        ));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void lastVoucher_twoUsers_oneIssuedOnePendingStock() throws Exception {
        insertSingleAvailableVoucher();

        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger failures = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(2);

        for (UUID userId : List.of(userA, userB)) {
            pool.submit(() -> {
                try {
                    start.await(5, TimeUnit.SECONDS);
                    tx.executeWithoutResult(s -> rewardEvaluationService.handleStampCollected(userId, campaignId));
                } catch (Exception e) {
                    failures.incrementAndGet();
                }
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));
        assertEquals(0, failures.get(), "No uncaught exceptions during concurrent evaluation");

        Long issued = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_rewards WHERE milestone_id = ? AND status = 'ISSUED'",
                Long.class, milestoneId);
        Long pending = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_rewards WHERE milestone_id = ? AND status = 'PENDING_STOCK'",
                Long.class, milestoneId);
        Long claimed = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM voucher_pool WHERE milestone_id = ? AND status = 'CLAIMED'",
                Long.class, milestoneId);
        Long assignedUnique = jdbcTemplate.queryForObject(
                "SELECT COUNT(DISTINCT assigned_user_reward_id) FROM voucher_pool WHERE milestone_id = ? AND assigned_user_reward_id IS NOT NULL",
                Long.class, milestoneId);

        assertEquals(1L, issued);
        assertEquals(1L, pending);
        assertEquals(1L, claimed);
        assertEquals(1L, assignedUnique);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void duplicateEvent_sameUser_onlyOneUserReward() throws Exception {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger failures = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(2);

        for (int i = 0; i < 2; i++) {
            pool.submit(() -> {
                try {
                    start.await(5, TimeUnit.SECONDS);
                    tx.executeWithoutResult(s -> rewardEvaluationService.handleStampCollected(userA, campaignId));
                } catch (Exception e) {
                    failures.incrementAndGet();
                }
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));

        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_rewards WHERE user_id = ? AND milestone_id = ?",
                Long.class, userA, milestoneId);
        assertEquals(1L, count, "Concurrent duplicate events must yield exactly one user_reward");
        // Losing racers may surface as caught unique violations; final cardinality is the contract.
        assertTrue(failures.get() <= 1, "Unexpected concurrent failures: " + failures.get());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void pendingStock_remainsAfterVoucherImport_optionA() {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(s -> rewardEvaluationService.handleStampCollected(userA, campaignId));

        Long pendingBefore = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_rewards WHERE user_id = ? AND status = 'PENDING_STOCK'",
                Long.class, userA);
        assertEquals(1L, pendingBefore);

        tx.executeWithoutResult(s -> voucherPoolCommandService.importVouchers(
                new ImportVouchersCommand(milestoneId, List.of("NEW-CODE-1", "NEW-CODE-2"), null)));

        Long pendingAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_rewards WHERE user_id = ? AND status = 'PENDING_STOCK'",
                Long.class, userA);
        Long issuedAfter = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_rewards WHERE user_id = ? AND status = 'ISSUED'",
                Long.class, userA);

        assertEquals(1L, pendingAfter, "Option A: import does not auto-allocate PENDING_STOCK");
        assertEquals(0L, issuedAfter);
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
