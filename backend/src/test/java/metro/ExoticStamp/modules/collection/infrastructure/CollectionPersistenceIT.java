package metro.ExoticStamp.modules.collection.infrastructure;

import metro.ExoticStamp.modules.collection.domain.model.Campaign;
import metro.ExoticStamp.modules.collection.domain.model.CollectMethod;
import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.repository.CampaignRepository;
import metro.ExoticStamp.modules.collection.domain.repository.UserStampRepository;
import metro.ExoticStamp.modules.collection.infrastructure.repository.CampaignRepositoryAdapter;
import metro.ExoticStamp.modules.collection.infrastructure.repository.JpaCampaignRepository;
import metro.ExoticStamp.modules.collection.infrastructure.repository.JpaUserStampRepository;
import metro.ExoticStamp.modules.collection.infrastructure.repository.UserStampRepositoryAdapter;
import org.springframework.boot.autoconfigure.domain.EntityScan;
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
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@EntityScan(basePackages = "metro.ExoticStamp.modules.collection.domain.model")
@EnableJpaRepositories(basePackageClasses = {JpaUserStampRepository.class, JpaCampaignRepository.class})
@Import({UserStampRepositoryAdapter.class, CampaignRepositoryAdapter.class})
class CollectionPersistenceIT {

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
                "INSERT INTO lines (id, code, name, total_stations, is_active) VALUES (?,?,?,?,?)",
                lineId, "L" + lineId.toString().substring(0, 4), "Test Line", 1, true);

        jdbcTemplate.update(
                "INSERT INTO stations (id, line_id, code, name, sequence, is_active, collector_count) VALUES (?,?,?,?,?,?,?)",
                stationId, lineId, "S1", "Station 1", 1, true, 0);

        jdbcTemplate.update(
                "INSERT INTO campaigns (id, code, name, description, start_date, end_date, is_active, line_id, is_default) VALUES (?,?,?,?,?,?,?,?,?)",
                campaignId, "CMP-" + campaignId.toString().substring(0, 8), "Camp", "d",
                now, now.plusYears(1), true, lineId, true);

        jdbcTemplate.update(
                "INSERT INTO stamp_designs (id, station_id, campaign_id, name, artwork_url, is_limited, is_active) VALUES (?,?,?,?,?,?,?)",
                stampDesignId, stationId, campaignId, "Design", "https://example.com/a.png", false, true);
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

        assertThrows(DataIntegrityViolationException.class, () -> userStampRepository.save(dup));
    }
}
