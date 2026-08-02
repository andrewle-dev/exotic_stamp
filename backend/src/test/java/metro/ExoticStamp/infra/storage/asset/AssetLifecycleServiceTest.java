package metro.ExoticStamp.infra.storage.asset;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import metro.ExoticStamp.infra.storage.PublicUrlResolver;
import metro.ExoticStamp.infra.storage.StorageMetrics;
import metro.ExoticStamp.infra.storage.StorageProperties;
import metro.ExoticStamp.infra.storage.StorageUploadResult;
import metro.ExoticStamp.infra.storage.StorageVisibility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AssetLifecycleServiceTest {

    @Mock
    private StoredAssetRepository storedAssetRepository;

    private AssetLifecycleService service;
    private StorageMetrics metrics;

    @BeforeEach
    void setUp() {
        StorageProperties props = new StorageProperties();
        props.setProvider("s3");
        props.setPublicBaseUrl("https://cdn.example.com");
        props.getCleanup().setOrphanRetention(Duration.ofDays(14));
        metrics = new StorageMetrics(new SimpleMeterRegistry());
        service = new AssetLifecycleService(
                storedAssetRepository, props, new PublicUrlResolver(props), metrics);
    }

    @Test
    void recordPending_persistsPendingRow() {
        when(storedAssetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        StorageUploadResult result = new StorageUploadResult(
                "public/stations/x/cover/a.png",
                "https://cdn.example.com/public/stations/x/cover/a.png",
                "image/png",
                10,
                "abc",
                StorageVisibility.PUBLIC,
                "s3");

        StoredAsset pending = service.recordPending(result, "station", UUID.randomUUID());

        assertThat(pending.getStatus()).isEqualTo(StoredAssetStatus.PENDING);
        verify(storedAssetRepository).save(any(StoredAsset.class));
    }

    @Test
    void orphanPrevious_marksExistingActiveAsOrphaned() {
        StoredAsset active = StoredAsset.builder()
                .id(UUID.randomUUID())
                .provider("s3")
                .objectKey("public/stations/x/cover/old.png")
                .publicUrl("https://cdn.example.com/public/stations/x/cover/old.png")
                .visibility(StorageVisibility.PUBLIC)
                .status(StoredAssetStatus.ACTIVE)
                .build();
        when(storedAssetRepository.findByPublicUrl(active.getPublicUrl())).thenReturn(Optional.of(active));
        when(storedAssetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.orphanPrevious(active.getPublicUrl(), null);

        ArgumentCaptor<StoredAsset> captor = ArgumentCaptor.forClass(StoredAsset.class);
        verify(storedAssetRepository).save(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(StoredAssetStatus.ORPHANED);
        assertThat(captor.getValue().getDeleteAfter()).isNotNull();
        assertThat(metrics).isNotNull();
    }
}
