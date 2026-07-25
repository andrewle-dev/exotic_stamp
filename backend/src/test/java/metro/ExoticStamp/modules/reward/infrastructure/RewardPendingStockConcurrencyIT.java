package metro.ExoticStamp.modules.reward.infrastructure;

import metro.ExoticStamp.modules.reward.application.port.RewardCachePort;
import metro.ExoticStamp.modules.reward.application.service.RewardEvaluationService;
import metro.ExoticStamp.modules.reward.application.service.RewardIssuancePolicyService;
import metro.ExoticStamp.modules.reward.application.service.VoucherAllocationService;
import metro.ExoticStamp.modules.reward.application.support.RewardAuditHelper;
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
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * PostgreSQL pending-stock concurrency proofs (Batch E.2 Phase 3). Latches only — no sleeps.
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
        RewardPendingStockConcurrencyIT.TestClockConfig.class
})
class RewardPendingStockConcurrencyIT {

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
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private ApplicationEventPublisher applicationEventPublisher;

    @MockBean private RewardCachePort rewardCachePort;
    @MockBean private RewardAuditHelper rewardAuditHelper;
    @MockBean private ApplicationEventPublisher eventPublisher;

    private UUID lineId;
    private UUID campaignId;
    private UUID milestoneId;
    private UUID userId;
    private UUID stationId;
    private UUID designId;

    @BeforeEach
    void seed() {
        lineId = UUID.randomUUID();
        campaignId = UUID.randomUUID();
        milestoneId = UUID.randomUUID();
        userId = UUID.randomUUID();
        stationId = UUID.randomUUID();
        designId = UUID.randomUUID();
        TransactionTemplate commit = new TransactionTemplate(transactionManager);
        commit.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        commit.executeWithoutResult(s -> insertBase());
    }

    private void insertBase() {
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
                lineId, "L" + lineId.toString().substring(0, 8), "Line", "Line", 1, "ACTIVE", 0);
        jdbcTemplate.update(
                "INSERT INTO stations (id, line_id, code, name, display_name, sort_order, status, collector_count) VALUES (?,?,?,?,?,?,?,?)",
                stationId, lineId, "S" + stationId.toString().substring(0, 7), "S1", "S1", 1, "ACTIVE", 0);
        jdbcTemplate.update(
                """
                INSERT INTO campaigns (
                    id, code, name, display_name, description, campaign_type, status,
                    start_at, end_at, priority, line_id, is_default
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                """,
                campaignId, "CMP-" + campaignId.toString().substring(0, 8), "Camp", "Camp", "d", "STANDARD", "ACTIVE",
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
                milestoneId, lineId, campaignId, "VM-" + milestoneId.toString().substring(0, 8), 1, "Voucher M", "desc",
                RewardType.VOUCHER.name(), "Voucher Prize", "ACTIVE", 0, true);
        jdbcTemplate.update(
                """
                INSERT INTO user_stamps (id, user_id, station_id, stamp_design_id, campaign_id, collected_at,
                gps_verified, collect_method, device_fingerprint, idempotency_key)
                VALUES (?,?,?,?,?,?,?,?::collect_method_enum,?,?)
                """,
                UUID.randomUUID(), userId, stationId, designId, campaignId, now,
                false, "NFC", "device", "fp-" + userId);
    }

    private UUID insertPendingReward() {
        UUID rewardId = UUID.randomUUID();
        TransactionTemplate commit = new TransactionTemplate(transactionManager);
        commit.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        commit.executeWithoutResult(s -> jdbcTemplate.update(
                """
                INSERT INTO user_rewards (id, user_id, campaign_id, milestone_id, issued_at, status)
                VALUES (?,?,?,?,?,?)
                """,
                rewardId, userId, campaignId, milestoneId, LocalDateTime.now(), "PENDING_STOCK"
        ));
        return rewardId;
    }

    private void insertAvailableVoucher(String code) {
        TransactionTemplate commit = new TransactionTemplate(transactionManager);
        commit.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        commit.executeWithoutResult(s -> jdbcTemplate.update(
                """
                INSERT INTO voucher_pool (id, milestone_id, code, status, is_redeemed, created_at)
                VALUES (?,?,?,?,?,?)
                """,
                UUID.randomUUID(), milestoneId, code, VoucherPoolStatus.AVAILABLE.name(), false, LocalDateTime.now()
        ));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void onePendingOneVoucher_fulfillsExactlyOnce() {
        UUID rewardId = insertPendingReward();
        insertAvailableVoucher("V-ONE-" + UUID.randomUUID().toString().substring(0, 8));

        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        AtomicReference<RewardEvaluationService.PendingStockResult> result = new AtomicReference<>();
        tx.executeWithoutResult(s -> result.set(rewardEvaluationService.fulfillPendingStock(rewardId)));

        assertEquals(RewardEvaluationService.PendingStockResult.FULFILLED, result.get());
        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM user_rewards WHERE id = ?", String.class, rewardId);
        assertEquals("ISSUED", status);
        Long linked = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM voucher_pool WHERE assigned_user_reward_id = ?", Long.class, rewardId);
        assertEquals(1L, linked);
        Long claimed = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM voucher_pool WHERE milestone_id = ? AND status = 'CLAIMED'",
                Long.class, milestoneId);
        assertEquals(1L, claimed);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void twoWorkersSamePending_oneWinnerNoDuplicateVoucher() throws Exception {
        UUID rewardId = insertPendingReward();
        insertAvailableVoucher("V-RACE-" + UUID.randomUUID().toString().substring(0, 8));

        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger fulfilled = new AtomicInteger();
        AtomicInteger stillNo = new AtomicInteger();
        AtomicInteger skipped = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        for (int i = 0; i < 2; i++) {
            pool.submit(() -> {
                try {
                    start.await(5, TimeUnit.SECONDS);
                    tx.executeWithoutResult(s -> {
                        var r = rewardEvaluationService.fulfillPendingStock(rewardId);
                        switch (r) {
                            case FULFILLED -> fulfilled.incrementAndGet();
                            case STILL_NO_STOCK -> stillNo.incrementAndGet();
                            case SKIPPED -> skipped.incrementAndGet();
                        }
                    });
                } catch (Exception ignored) {
                    // unique race may surface; cardinality assertions below are the contract
                }
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));

        assertEquals(1, fulfilled.get());
        Long claimed = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM voucher_pool WHERE milestone_id = ? AND status = 'CLAIMED'",
                Long.class, milestoneId);
        assertEquals(1L, claimed);
        Long issued = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_rewards WHERE id = ? AND status = 'ISSUED'", Long.class, rewardId);
        assertEquals(1L, issued);
        // Issued event published at most once after commit (MockBean publisher).
        verify(eventPublisher, org.mockito.Mockito.atMost(1)).publishEvent(any());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void multiplePendingFewerVouchers_noDuplicateAndRemainPending() {
        UUID r1 = insertPendingReward();
        UUID user2 = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        TransactionTemplate commit = new TransactionTemplate(transactionManager);
        commit.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        commit.executeWithoutResult(s -> {
            jdbcTemplate.update(
                    """
                    INSERT INTO users (id, username, email, phone_number, password, status, token_version, created_at)
                    VALUES (?,?,?,?,?,?,?,?)
                    """,
                    user2, "u2-" + user2.toString().substring(0, 8),
                    "u2-" + user2.toString().substring(0, 8) + "@example.com",
                    "+1666" + user2.toString().replace("-", "").substring(0, 7),
                    "hashed", "ACTIVE", 0L, now);
            jdbcTemplate.update(
                    """
                    INSERT INTO user_stamps (id, user_id, station_id, stamp_design_id, campaign_id, collected_at,
                    gps_verified, collect_method, device_fingerprint, idempotency_key)
                    VALUES (?,?,?,?,?,?,?,?::collect_method_enum,?,?)
                    """,
                    UUID.randomUUID(), user2, stationId, designId, campaignId, now,
                    false, "NFC", "device", "fp-" + user2);
            jdbcTemplate.update(
                    """
                    INSERT INTO user_rewards (id, user_id, campaign_id, milestone_id, issued_at, status)
                    VALUES (?,?,?,?,?,?)
                    """,
                    UUID.randomUUID(), user2, campaignId, milestoneId, now, "PENDING_STOCK");
        });
        insertAvailableVoucher("ONLY-" + UUID.randomUUID().toString().substring(0, 8));

        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        List<UUID> pendingIds = jdbcTemplate.queryForList(
                "SELECT id FROM user_rewards WHERE milestone_id = ? AND status = 'PENDING_STOCK'",
                UUID.class, milestoneId);
        assertEquals(2, pendingIds.size());
        for (UUID id : pendingIds) {
            tx.executeWithoutResult(s -> rewardEvaluationService.fulfillPendingStock(id));
        }

        Long claimed = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM voucher_pool WHERE milestone_id = ? AND status = 'CLAIMED'",
                Long.class, milestoneId);
        Long issued = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_rewards WHERE milestone_id = ? AND status = 'ISSUED'",
                Long.class, milestoneId);
        Long pending = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_rewards WHERE milestone_id = ? AND status = 'PENDING_STOCK'",
                Long.class, milestoneId);
        assertEquals(1L, claimed);
        assertEquals(1L, issued);
        assertEquals(1L, pending);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void alreadyIssued_processedAgain_noStateChangeNoNewEvent() {
        UUID rewardId = insertPendingReward();
        insertAvailableVoucher("V-ISSUED-" + UUID.randomUUID().toString().substring(0, 8));
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(s -> assertEquals(
                RewardEvaluationService.PendingStockResult.FULFILLED,
                rewardEvaluationService.fulfillPendingStock(rewardId)));
        resetPublisherInteractions();
        tx.executeWithoutResult(s -> assertEquals(
                RewardEvaluationService.PendingStockResult.SKIPPED,
                rewardEvaluationService.fulfillPendingStock(rewardId)));
        Long claimed = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM voucher_pool WHERE milestone_id = ? AND status = 'CLAIMED'",
                Long.class, milestoneId);
        assertEquals(1L, claimed);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void noStock_returnsStillNoStock_notSystemFailure() {
        UUID rewardId = insertPendingReward();
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        AtomicReference<RewardEvaluationService.PendingStockResult> result = new AtomicReference<>();
        assertDoesNotThrow(() -> tx.executeWithoutResult(s ->
                result.set(rewardEvaluationService.fulfillPendingStock(rewardId))));
        assertEquals(RewardEvaluationService.PendingStockResult.STILL_NO_STOCK, result.get());
        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM user_rewards WHERE id = ?", String.class, rewardId);
        assertEquals("PENDING_STOCK", status);
    }

    private void resetPublisherInteractions() {
        org.mockito.Mockito.clearInvocations(eventPublisher);
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