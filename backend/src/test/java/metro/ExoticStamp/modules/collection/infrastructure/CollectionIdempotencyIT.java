package metro.ExoticStamp.modules.collection.infrastructure;

import metro.ExoticStamp.modules.collection.application.support.CollectIdempotencyFingerprint;
import metro.ExoticStamp.modules.collection.application.support.CollectionPolicyService;
import metro.ExoticStamp.modules.collection.application.support.CollectionRuntimeAuditHelper;
import metro.ExoticStamp.modules.collection.domain.exception.IdempotencyConflictException;
import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.infrastructure.repository.JpaUserStampRepository;
import metro.ExoticStamp.modules.collection.infrastructure.repository.UserStampRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
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

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        CollectionIdempotencyIT.PersistenceTestConfig.class,
        UserStampRepositoryAdapter.class,
        CollectionPolicyService.class
})
class CollectionIdempotencyIT {

    @org.springframework.boot.test.context.TestConfiguration
    @EntityScan(basePackageClasses = {
            metro.ExoticStamp.modules.collection.domain.model.UserStamp.class,
            metro.ExoticStamp.modules.collection.domain.model.Campaign.class,
            metro.ExoticStamp.modules.collection.domain.model.StampDesign.class,
            metro.ExoticStamp.modules.collection.infrastructure.repository.CampaignStationEntity.class
    })
    @org.springframework.data.jpa.repository.config.EnableJpaRepositories(basePackageClasses = {
            JpaUserStampRepository.class
    })
    static class PersistenceTestConfig {
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> {
            String url = postgres.getJdbcUrl();
            return url + (url.contains("?") ? "&" : "?") + "stringtype=unspecified";
        });
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired JdbcTemplate jdbc;
    @Autowired UserStampRepositoryAdapter userStampRepository;
    @Autowired CollectionPolicyService policyService;
    @Autowired PlatformTransactionManager txManager;
    @MockBean CollectionRuntimeAuditHelper auditHelper;

    UUID userA;
    UUID userB;
    UUID station1;
    UUID station2;
    UUID campaign1;
    UUID campaign2;
    UUID design1;
    UUID design2;

    @BeforeEach
    void seed() {
        userA = UUID.randomUUID();
        userB = UUID.randomUUID();
        station1 = UUID.randomUUID();
        station2 = UUID.randomUUID();
        campaign1 = UUID.randomUUID();
        campaign2 = UUID.randomUUID();
        design1 = UUID.randomUUID();
        design2 = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        UUID lineId = UUID.randomUUID();
        for (UUID u : new UUID[]{userA, userB}) {
            jdbc.update("""
                    INSERT INTO users (id, username, email, phone_number, password, status, token_version, created_at)
                    VALUES (?,?,?,?,?,?,?,?)
                    """, u, "u-" + u.toString().substring(0, 8), "u-" + u.toString().substring(0, 8) + "@ex.com",
                    "+1555" + u.toString().replace("-", "").substring(0, 7), "x", "ACTIVE", 0L, now);
        }
        String suffix = lineId.toString().substring(0, 8);
        jdbc.update("INSERT INTO lines (id, code, name, display_name, total_stations, status, sort_order) VALUES (?,?,?,?,?,?,?)",
                lineId, "L" + suffix, "L", "L", 2, "ACTIVE", 0);
        jdbc.update("INSERT INTO stations (id, line_id, code, name, display_name, sort_order, status, collector_count) VALUES (?,?,?,?,?,?,?,?)",
                station1, lineId, "S1" + suffix, "S1", "S1", 1, "ACTIVE", 0);
        jdbc.update("INSERT INTO stations (id, line_id, code, name, display_name, sort_order, status, collector_count) VALUES (?,?,?,?,?,?,?,?)",
                station2, lineId, "S2" + suffix, "S2", "S2", 2, "ACTIVE", 0);
        jdbc.update("""
                INSERT INTO campaigns (id, code, name, display_name, campaign_type, status, start_at, end_at, priority, line_id, is_default)
                VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """, campaign1, "C1" + suffix, "C1", "C1", "STANDARD", "ACTIVE", now, now.plusYears(1), 0, lineId, true);
        jdbc.update("""
                INSERT INTO campaigns (id, code, name, display_name, campaign_type, status, start_at, end_at, priority, line_id, is_default)
                VALUES (?,?,?,?,?,?,?,?,?,?,?)
                """, campaign2, "C2" + suffix, "C2", "C2", "STANDARD", "ACTIVE", now, now.plusYears(1), 0, lineId, false);
        jdbc.update("""
                INSERT INTO stamp_designs (id, station_id, campaign_id, name, image_url, rarity, status, sort_order, is_limited)
                VALUES (?,?,?,?,?,?,?,?,?)
                """, design1, station1, campaign1, "D1", "https://x/1.png", "COMMON", "ACTIVE", 0, false);
        jdbc.update("""
                INSERT INTO stamp_designs (id, station_id, campaign_id, name, image_url, rarity, status, sort_order, is_limited)
                VALUES (?,?,?,?,?,?,?,?,?)
                """, design2, station2, campaign1, "D2", "https://x/2.png", "COMMON", "ACTIVE", 0, false);
    }

    private UserStamp stamp(UUID userId, UUID stationId, UUID campaignId, UUID designId, String key, String fp) {
        return UserStamp.builder()
                .userId(userId).stationId(stationId).campaignId(campaignId).stampDesignId(designId)
                .collectedAt(LocalDateTime.now()).gpsVerified(false).collectMethod(CollectMethod.NFC)
                .deviceFingerprint("device").idempotencyKey(key).idempotencyFingerprint(fp)
                .collectionPolicy("MVP_ONCE_PER_STATION_CAMPAIGN").sourceScanType("NFC").build();
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void sameKeySameStationCampaign_replays() {
        String key = UUID.randomUUID().toString();
        String fp = CollectIdempotencyFingerprint.compute(userA, station1, campaign1, "NFC");
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.executeWithoutResult(s -> userStampRepository.save(stamp(userA, station1, campaign1, design1, key, fp)));
        assertTrue(policyService.resolveIdempotentReplay(key, userA, fp, station1, campaign1).isPresent());
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void sameKeyDifferentStation_conflicts() {
        String key = UUID.randomUUID().toString();
        String fp1 = CollectIdempotencyFingerprint.compute(userA, station1, campaign1, "NFC");
        String fp2 = CollectIdempotencyFingerprint.compute(userA, station2, campaign1, "NFC");
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.executeWithoutResult(s -> userStampRepository.save(stamp(userA, station1, campaign1, design1, key, fp1)));
        assertThrows(IdempotencyConflictException.class,
                () -> policyService.resolveIdempotentReplay(key, userA, fp2, station2, campaign1));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void sameKeyDifferentCampaign_conflicts() {
        String key = UUID.randomUUID().toString();
        String fp1 = CollectIdempotencyFingerprint.compute(userA, station1, campaign1, "NFC");
        String fp2 = CollectIdempotencyFingerprint.compute(userA, station1, campaign2, "NFC");
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.executeWithoutResult(s -> userStampRepository.save(stamp(userA, station1, campaign1, design1, key, fp1)));
        assertThrows(IdempotencyConflictException.class,
                () -> policyService.resolveIdempotentReplay(key, userA, fp2, station1, campaign2));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void concurrentSameKeySameOp_oneRow() throws Exception {
        String key = UUID.randomUUID().toString();
        String fp = CollectIdempotencyFingerprint.compute(userA, station1, campaign1, "NFC");
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger ok = new AtomicInteger();
        AtomicInteger dup = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        for (int i = 0; i < 2; i++) {
            pool.submit(() -> {
                try {
                    start.await(5, TimeUnit.SECONDS);
                    tx.executeWithoutResult(s -> userStampRepository.save(
                            stamp(userA, station1, campaign1, design1, key, fp)));
                    ok.incrementAndGet();
                } catch (DataIntegrityViolationException e) {
                    dup.incrementAndGet();
                } catch (Exception e) {
                    if (e.getCause() instanceof DataIntegrityViolationException) {
                        dup.incrementAndGet();
                    }
                }
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(20, TimeUnit.SECONDS));
        Long cnt = jdbc.queryForObject(
                "SELECT COUNT(*) FROM user_stamps WHERE user_id = ? AND idempotency_key = ?",
                Long.class, userA, key);
        assertEquals(1L, cnt);
        assertEquals(1, ok.get());
        assertTrue(dup.get() >= 1);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void differentUsersSameKey_allowed() {
        String key = UUID.randomUUID().toString();
        String fpA = CollectIdempotencyFingerprint.compute(userA, station1, campaign1, "NFC");
        String fpB = CollectIdempotencyFingerprint.compute(userB, station1, campaign1, "NFC");
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.executeWithoutResult(s -> {
            userStampRepository.save(stamp(userA, station1, campaign1, design1, key, fpA));
            userStampRepository.save(stamp(userB, station1, campaign1, design1, key, fpB));
        });
        Long cnt = jdbc.queryForObject("SELECT COUNT(*) FROM user_stamps WHERE idempotency_key = ?", Long.class, key);
        assertEquals(2L, cnt);
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void legacyNullFingerprint_differentStation_conflicts() {
        String key = UUID.randomUUID().toString();
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.executeWithoutResult(s -> userStampRepository.save(stamp(userA, station1, campaign1, design1, key, null)));
        String fp = CollectIdempotencyFingerprint.compute(userA, station2, campaign1, "NFC");
        assertThrows(IdempotencyConflictException.class,
                () -> policyService.resolveIdempotentReplay(key, userA, fp, station2, campaign1));
    }

    @Test
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void uniqueViolationMessage_containsConstraintInternally_notRequiredInApi() {
        String key = UUID.randomUUID().toString();
        String fp1 = CollectIdempotencyFingerprint.compute(userA, station1, campaign1, "NFC");
        String fp2 = CollectIdempotencyFingerprint.compute(userA, station2, campaign1, "NFC");
        TransactionTemplate tx = new TransactionTemplate(txManager);
        tx.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        tx.executeWithoutResult(s -> userStampRepository.save(stamp(userA, station1, campaign1, design1, key, fp1)));
        DataIntegrityViolationException ex = assertThrows(DataIntegrityViolationException.class, () ->
                tx.executeWithoutResult(s -> userStampRepository.save(
                        stamp(userA, station2, campaign1, design2, key, fp2))));
        String msg = ex.getMostSpecificCause().getMessage();
        assertNotNull(msg);
        assertTrue(msg.contains("uq_user_stamps_user_idempotency"), "msg=" + msg);
    }
}
