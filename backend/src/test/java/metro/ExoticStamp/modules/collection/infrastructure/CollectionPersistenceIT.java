package metro.ExoticStamp.modules.collection.infrastructure;

import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import metro.ExoticStamp.modules.collection.infrastructure.repository.CampaignRepositoryAdapter;
import metro.ExoticStamp.modules.collection.infrastructure.repository.CampaignStationEntity;
import metro.ExoticStamp.modules.collection.infrastructure.repository.JpaCampaignRepository;
import metro.ExoticStamp.modules.collection.infrastructure.repository.JpaUserStampRepository;
import metro.ExoticStamp.modules.collection.infrastructure.repository.UserStampRepositoryAdapter;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({
        CollectionPersistenceIT.PersistenceTestConfig.class,
        UserStampRepositoryAdapter.class,
        CampaignRepositoryAdapter.class
})
class CollectionPersistenceIT {

    @TestConfiguration
    @EntityScan(basePackageClasses = {
            Campaign.class,
            UserStamp.class,
            metro.ExoticStamp.modules.collection.domain.model.StampDesign.class,
            CampaignStationEntity.class
    })
    @EnableJpaRepositories(basePackageClasses = {
            JpaUserStampRepository.class,
            JpaCampaignRepository.class
    })
    static class PersistenceTestConfig {
    }

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void registerPg(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> {
            String url = postgres.getJdbcUrl();
            String sep = url.contains("?") ? "&" : "?";
            // Allow VARCHAR binds to PG native enums without production @JdbcType changes
            return url + sep + "stringtype=unspecified";
        });
        r.add("spring.datasource.username", postgres::getUsername);
        r.add("spring.datasource.password", postgres::getPassword);
        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    }

    @Autowired
    private UserStampRepository userStampRepository;

    @Autowired
    private CampaignRepository campaignRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JpaUserStampRepository jpaUserStampRepository;

    private UUID lineId;
    private UUID stationId;
    private UUID campaignId;
    private UUID stampDesignId;
    private UUID userId;

    @BeforeEach
    void seed() {
        lineId = UUID.randomUUID();
        stationId = UUID.randomUUID();
        campaignId = UUID.randomUUID();
        stampDesignId = UUID.randomUUID();
        userId = UUID.randomUUID();

        LocalDateTime now = LocalDateTime.now();

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

        jdbcTemplate.update(
                """
                INSERT INTO lines (id, code, name, display_name, total_stations, status, sort_order)
                VALUES (?,?,?,?,?,?,?)
                """,
                lineId, "L" + lineId.toString().substring(0, 4), "Test Line", "Test Line", 1, "ACTIVE", 0);

        jdbcTemplate.update(
                """
                INSERT INTO stations (id, line_id, code, name, display_name, sort_order, status, collector_count)
                VALUES (?,?,?,?,?,?,?,?)
                """,
                stationId, lineId, "S1", "Station 1", "Station 1", 1, "ACTIVE", 0);

        jdbcTemplate.update(
                """
                INSERT INTO campaigns (
                    id, code, name, display_name, description, campaign_type, status,
                    start_at, end_at, priority, line_id, is_default
                ) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)
                """,
                campaignId, "CMP-" + campaignId.toString().substring(0, 8), "Camp", "Camp", "d",
                "STANDARD", "ACTIVE", now, now.plusYears(1), 0, lineId, true);

        jdbcTemplate.update(
                """
                INSERT INTO stamp_designs (
                    id, station_id, campaign_id, name, image_url, rarity, status, sort_order, is_limited
                ) VALUES (?,?,?,?,?,?,?,?,?)
                """,
                stampDesignId, stationId, campaignId, "Design", "https://example.com/a.png",
                "COMMON", "ACTIVE", 0, false);
    }

    @Test
    void campaign_findDefaultByLineId_returnsPersisted() {
        Optional<Campaign> found = campaignRepository.findDefaultByLineId(lineId);
        assertTrue(found.isPresent());
        assertEquals(campaignId, found.get().getId());
        assertEquals(lineId, found.get().getLineId());
        assertTrue(found.get().isDefault());
    }

    @Test
    void userStamp_save_persists() {
        UserStamp us = UserStamp.builder()
                .userId(userId)
                .stationId(stationId)
                .campaignId(campaignId)
                .stampDesignId(stampDesignId)
                .collectedAt(LocalDateTime.now())
                .gpsVerified(false)
                .collectMethod(CollectMethod.NFC)
                .deviceFingerprint("1234567890")
                .collectionPolicy("MVP_ONCE_PER_STATION_CAMPAIGN")
                .idempotencyKey(UUID.randomUUID().toString())
                .latitude(BigDecimal.ZERO)
                .longitude(BigDecimal.ZERO)
                .createdAt(LocalDateTime.now())
                .build();

        UserStamp saved = userStampRepository.save(us);
        assertNotNull(saved.getId());
        assertEquals(1, jpaUserStampRepository.findAll().stream().filter(u -> u.getId().equals(saved.getId())).count());
    }

    @Test
    void userStamp_duplicateUserStationCampaign_throwsDataIntegrityViolation() {
        UserStamp base = UserStamp.builder()
                .userId(userId)
                .stationId(stationId)
                .campaignId(campaignId)
                .stampDesignId(stampDesignId)
                .collectedAt(LocalDateTime.now())
                .gpsVerified(false)
                .collectMethod(CollectMethod.NFC)
                .deviceFingerprint("1234567890")
                .collectionPolicy("MVP_ONCE_PER_STATION_CAMPAIGN")
                .idempotencyKey(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .build();
        userStampRepository.save(base);
        jpaUserStampRepository.flush();

        UserStamp dup = UserStamp.builder()
                .userId(userId)
                .stationId(stationId)
                .campaignId(campaignId)
                .stampDesignId(stampDesignId)
                .collectedAt(LocalDateTime.now())
                .gpsVerified(false)
                .collectMethod(CollectMethod.QR)
                .deviceFingerprint("1234567890")
                .collectionPolicy("MVP_ONCE_PER_STATION_CAMPAIGN")
                .idempotencyKey(UUID.randomUUID().toString())
                .createdAt(LocalDateTime.now())
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> {
            userStampRepository.save(dup);
            jpaUserStampRepository.flush();
        });
    }
}
