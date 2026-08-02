package metro.ExoticStamp.infra.storage.asset;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import metro.ExoticStamp.infra.storage.StorageMetrics;
import metro.ExoticStamp.infra.storage.StorageProperties;
import metro.ExoticStamp.infra.storage.StorageService;
import metro.ExoticStamp.infra.storage.StorageVisibility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrphanCleanupJobTest {

    @Mock
    private StoredAssetRepository storedAssetRepository;
    @Mock
    private StorageService storageService;

    private StorageProperties storageProperties;
    private OrphanCleanupJob job;

    @BeforeEach
    void setUp() {
        storageProperties = new StorageProperties();
        storageProperties.getCleanup().setBatchSize(50);
        storageProperties.getCleanup().setMaxRunDuration(Duration.ofSeconds(30));
        storageProperties.getCleanup().setOrphanRetention(Duration.ofDays(14));
        storageProperties.getCleanup().setDryRun(false);
        job = new OrphanCleanupJob(
                storedAssetRepository,
                storageService,
                storageProperties,
                new StorageMetrics(new SimpleMeterRegistry()));
    }

    @Test
    void activeReferencedObject_isNeverDeleted() {
        StoredAsset orphan = orphanAsset("public/stations/1/cover/a.jpg");
        when(storedAssetRepository.findByStatusAndDeleteAfterBefore(
                eq(StoredAssetStatus.ORPHANED), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(orphan));
        when(storedAssetRepository.existsByObjectKeyAndStatus(
                orphan.getObjectKey(), StoredAssetStatus.ACTIVE))
                .thenReturn(true);
        when(storedAssetRepository.findStalePending(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of());

        OrphanCleanupJob.CleanupResult result = job.runCleanup(false);

        assertThat(result.deleted()).isZero();
        verify(storageService, never()).delete(any());
        verify(storedAssetRepository, never()).delete(any(StoredAsset.class));
    }

    @Test
    void dryRun_doesNotDeleteStorageOrRow() {
        StoredAsset orphan = orphanAsset("public/stations/1/cover/b.jpg");
        when(storedAssetRepository.findByStatusAndDeleteAfterBefore(
                eq(StoredAssetStatus.ORPHANED), any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of(orphan));
        when(storedAssetRepository.existsByObjectKeyAndStatus(
                orphan.getObjectKey(), StoredAssetStatus.ACTIVE))
                .thenReturn(false);
        when(storedAssetRepository.findStalePending(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of());

        OrphanCleanupJob.CleanupResult result = job.runCleanup(true);

        assertThat(result.dryRun()).isTrue();
        assertThat(result.deleted()).isZero();
        verify(storageService, never()).delete(any());
        verify(storedAssetRepository, never()).delete(any(StoredAsset.class));
    }

    @Test
    void concurrentRun_skipsIdempotently() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        when(storedAssetRepository.findByStatusAndDeleteAfterBefore(
                eq(StoredAssetStatus.ORPHANED), any(LocalDateTime.class), any(Pageable.class)))
                .thenAnswer(inv -> {
                    entered.countDown();
                    assertThat(release.await(5, TimeUnit.SECONDS)).isTrue();
                    return List.of();
                });
        when(storedAssetRepository.findStalePending(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(List.of());

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            Future<OrphanCleanupJob.CleanupResult> first = pool.submit(() -> job.runCleanup(false));
            assertThat(entered.await(5, TimeUnit.SECONDS)).isTrue();

            OrphanCleanupJob.CleanupResult skipped = job.runCleanup(false);
            assertThat(skipped.skipped()).isTrue();
            assertThat(skipped.skipReason()).isEqualTo("already-running");

            release.countDown();
            assertThat(first.get(5, TimeUnit.SECONDS).skipped()).isFalse();
        } finally {
            pool.shutdownNow();
        }
    }

    private static StoredAsset orphanAsset(String key) {
        return StoredAsset.builder()
                .id(UUID.randomUUID())
                .provider("s3")
                .objectKey(key)
                .visibility(StorageVisibility.PUBLIC)
                .status(StoredAssetStatus.ORPHANED)
                .createdAt(LocalDateTime.now().minusDays(20))
                .orphanedAt(LocalDateTime.now().minusDays(15))
                .deleteAfter(LocalDateTime.now().minusDays(1))
                .build();
    }
}
