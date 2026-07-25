package metro.ExoticStamp.modules.reward.infrastructure;

import metro.ExoticStamp.modules.reward.application.port.RewardReconcileCandidatePort;
import metro.ExoticStamp.modules.reward.application.service.RewardEvaluationService;
import metro.ExoticStamp.modules.reward.application.service.RewardReconcileService;
import metro.ExoticStamp.modules.reward.config.RewardProperties;
import metro.ExoticStamp.modules.reward.infrastructure.repository.RewardReconcileAdvisoryLockAdapter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * Two independent physical PostgreSQL sessions prove multi-instance reconcile exclusivity
 * via session advisory lock (Batch E.2 Phase 2). Uses latches — no arbitrary sleeps.
 */
@JdbcTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        RewardReconcileAdvisoryLockAdapter.class,
        RewardReconcileService.class,
        RewardReconcileAdvisoryLockIT.TestConfig.class
})
class RewardReconcileAdvisoryLockIT {

    static final long LOCK_KEY = 0x4553525245434F4EL;

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void registerPg(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", postgres::getJdbcUrl);
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired private DataSource dataSource;
    @Autowired private RewardReconcileAdvisoryLockAdapter lockAdapter;
    @Autowired private RewardReconcileService rewardReconcileService;

    @MockBean private RewardReconcileCandidatePort candidatePort;
    @MockBean private RewardEvaluationService rewardEvaluationService;

    @Test
    void twoWorkers_secondBusyUntilFirstReleases() throws Exception {
        CountDownLatch aHolds = new CountDownLatch(1);
        CountDownLatch bTried = new CountDownLatch(1);
        CountDownLatch releaseA = new CountDownLatch(1);
        CountDownLatch aReleased = new CountDownLatch(1);
        AtomicBoolean bFirstAcquire = new AtomicBoolean(true);
        AtomicBoolean bSecondAcquire = new AtomicBoolean(false);
        AtomicBoolean aAcquired = new AtomicBoolean(false);

        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.submit(() -> {
            assertTrue(lockAdapter.tryAcquire());
            aAcquired.set(true);
            aHolds.countDown();
            try {
                assertTrue(releaseA.await(20, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lockAdapter.release();
                aReleased.countDown();
            }
        });
        pool.submit(() -> {
            try {
                assertTrue(aHolds.await(20, TimeUnit.SECONDS));
                bFirstAcquire.set(lockAdapter.tryAcquire());
                bTried.countDown();
                assertTrue(releaseA.await(20, TimeUnit.SECONDS));
                // Wait until A has actually released (same latch wake races with A's finally).
                assertTrue(aReleased.await(20, TimeUnit.SECONDS));
                bSecondAcquire.set(lockAdapter.tryAcquire());
                lockAdapter.release();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        assertTrue(aHolds.await(20, TimeUnit.SECONDS));
        assertTrue(bTried.await(20, TimeUnit.SECONDS));
        assertTrue(aAcquired.get());
        assertFalse(bFirstAcquire.get(), "B must not acquire while A holds the session lock");
        releaseA.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));
        assertTrue(bSecondAcquire.get(), "B acquires after A releases");
    }

    @Test
    void closingConnectionA_releasesSessionLockForB() throws Exception {
        try (Connection a = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
            assertTrue(tryLock(a));
            // While A holds: adapter on another physical session cannot acquire.
            RewardReconcileAdvisoryLockAdapter other = new RewardReconcileAdvisoryLockAdapter(dataSource);
            assertFalse(other.tryAcquire());
            // Closing A drops the TCP session → PG releases advisory lock.
        }
        RewardReconcileAdvisoryLockAdapter b = new RewardReconcileAdvisoryLockAdapter(dataSource);
        assertTrue(b.tryAcquire());
        b.release();
    }

    @Test
    void lockAndUnlockUseSamePhysicalSession() throws Exception {
        assertTrue(lockAdapter.tryAcquire());
        // Re-acquire on same thread/session is idempotent (heldConnection non-null).
        assertTrue(lockAdapter.tryAcquire());
        lockAdapter.release();
        // After release, a raw second session can take the lock.
        try (Connection c = DriverManager.getConnection(
                postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword())) {
            assertTrue(tryLock(c));
            unlock(c);
        }
    }

    @Test
    void exceptionInsideReconcile_stillReleasesLock() {
        when(candidatePort.findMissingRewardCandidates(any(), anyInt()))
                .thenThrow(new RuntimeException("boom"));

        assertThrows(RuntimeException.class, () -> rewardReconcileService.runReconcile());

        RewardReconcileAdvisoryLockAdapter probe = new RewardReconcileAdvisoryLockAdapter(dataSource);
        assertTrue(probe.tryAcquire(), "finally must release advisory lock after exception");
        probe.release();
    }

    @Test
    void scheduledAndAdminShareSameLockKey_busyWhenHeld() throws Exception {
        CountDownLatch held = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(1);
        AtomicReference<RewardReconcileService.ReconcileResult> busy = new AtomicReference<>();

        when(candidatePort.findMissingRewardCandidates(any(), anyInt())).thenReturn(List.of());
        when(candidatePort.claimPendingStockRewardIds(any(), anyInt())).thenReturn(List.of());
        when(candidatePort.peekPendingStockRewardIds(any(), anyInt())).thenReturn(List.of());

        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.submit(() -> {
            assertTrue(lockAdapter.tryAcquire());
            held.countDown();
            try {
                assertTrue(done.await(20, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lockAdapter.release();
            }
        });
        pool.submit(() -> {
            try {
                assertTrue(held.await(20, TimeUnit.SECONDS));
                busy.set(rewardReconcileService.runReconcile(
                        RewardReconcileService.ReconcileRequest.admin(10, false, UUID.randomUUID())));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        });
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));
        assertNotNull(busy.get());
        assertTrue(busy.get().skipped());
        assertEquals("already-running", busy.get().skipReason());
    }

    @Test
    void dryRun_stillTakesLock_andBlocksPeer() throws Exception {
        when(candidatePort.findMissingRewardCandidates(any(), anyInt())).thenReturn(List.of());
        when(candidatePort.peekPendingStockRewardIds(any(), anyInt())).thenReturn(List.of());

        CountDownLatch dryRunning = new CountDownLatch(1);
        CountDownLatch peerTried = new CountDownLatch(1);
        CountDownLatch finishDry = new CountDownLatch(1);
        AtomicBoolean peerGotLock = new AtomicBoolean(true);

        // Slow dry-run: hold by acquiring lock then blocking until peer observes busy.
        ExecutorService pool = Executors.newFixedThreadPool(2);
        pool.submit(() -> {
            assertTrue(lockAdapter.tryAcquire());
            dryRunning.countDown();
            try {
                assertTrue(finishDry.await(20, TimeUnit.SECONDS));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                lockAdapter.release();
            }
        });
        pool.submit(() -> {
            try {
                assertTrue(dryRunning.await(20, TimeUnit.SECONDS));
                var result = rewardReconcileService.runReconcile(
                        RewardReconcileService.ReconcileRequest.admin(5, true, UUID.randomUUID()));
                peerGotLock.set(!result.skipped());
                peerTried.countDown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                finishDry.countDown();
            }
        });
        assertTrue(peerTried.await(20, TimeUnit.SECONDS));
        pool.shutdown();
        assertTrue(pool.awaitTermination(30, TimeUnit.SECONDS));
        assertFalse(peerGotLock.get(), "Dry-run holder blocks peer (same lock key policy)");
    }

    private static boolean tryLock(Connection c) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("SELECT pg_try_advisory_lock(?)")) {
            ps.setLong(1, LOCK_KEY);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getBoolean(1);
            }
        }
    }

    private static void unlock(Connection c) throws Exception {
        try (PreparedStatement ps = c.prepareStatement("SELECT pg_advisory_unlock(?)")) {
            ps.setLong(1, LOCK_KEY);
            ps.executeQuery().close();
        }
    }

    @Configuration
    static class TestConfig {
        @Bean
        io.micrometer.core.instrument.MeterRegistry meterRegistry() {
            return new io.micrometer.core.instrument.simple.SimpleMeterRegistry();
        }

        @Bean
        RewardProperties rewardProperties() {
            RewardProperties props = new RewardProperties();
            props.setReconcileLookback(Duration.ofHours(48));
            props.setReconcileBatchSize(10);
            props.setReconcileMaxDuration(Duration.ofSeconds(30));
            props.setReconcileMaxBatches(3);
            props.setReconcileMaxBatchSize(100);
            return props;
        }
    }
}