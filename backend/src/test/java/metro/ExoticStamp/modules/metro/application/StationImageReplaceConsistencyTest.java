package metro.ExoticStamp.modules.metro.application;

import metro.ExoticStamp.common.exceptions.storage.ConcurrentAssetReplaceException;
import metro.ExoticStamp.common.exceptions.storage.InvalidImageTypeException;
import metro.ExoticStamp.common.exceptions.storage.StorageWriteFailedException;
import metro.ExoticStamp.infra.storage.FileValidator;
import metro.ExoticStamp.infra.storage.StorageProperties;
import metro.ExoticStamp.infra.storage.StorageService;
import metro.ExoticStamp.infra.storage.StorageUploadRequest;
import metro.ExoticStamp.infra.storage.StorageUploadResult;
import metro.ExoticStamp.infra.storage.StorageVisibility;
import metro.ExoticStamp.infra.storage.asset.AssetLifecycleService;
import metro.ExoticStamp.infra.storage.asset.StoredAsset;
import metro.ExoticStamp.infra.storage.asset.StoredAssetStatus;
import metro.ExoticStamp.modules.metro.application.mapper.MetroAppMapper;
import metro.ExoticStamp.modules.metro.application.port.StationCachePort;
import metro.ExoticStamp.modules.metro.application.support.MetroAuditHelper;
import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanKeyStatus;
import metro.ExoticStamp.modules.metro.domain.model.Station;
import metro.ExoticStamp.modules.metro.domain.repository.LineRepository;
import metro.ExoticStamp.modules.metro.domain.repository.StationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StationImageReplaceConsistencyTest {

    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");
    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");

    @Mock private LineRepository lineRepository;
    @Mock private StationRepository stationRepository;
    @Mock private StationCachePort stationCachePort;
    @Mock private MetroAppMapper mapper;
    @Mock private StorageService storageService;
    @Mock private AssetLifecycleService assetLifecycleService;
    @Mock private StationImagePointerService stationImagePointerService;
    @Mock private ApplicationEventPublisher eventPublisher;
    @Mock private MetroAuditHelper metroAuditHelper;

    private StationCommandService stationCommandService;

    @BeforeEach
    void setUp() {
        StorageProperties props = new StorageProperties();
        props.getFile().setMaxSizeMb(5);
        props.getFile().setAllowedTypes(List.of("image/jpeg", "image/png", "image/webp"));
        stationCommandService = new StationCommandService(
                lineRepository, stationRepository, stationCachePort, mapper,
                storageService, new FileValidator(props), assetLifecycleService,
                stationImagePointerService, eventPublisher, metroAuditHelper);
    }

    @Test
    void validationFailure_neverCallsStorage() {
        MockMultipartFile gif = new MockMultipartFile("file", "a.gif", "image/gif", new byte[10]);
        assertThrows(InvalidImageTypeException.class,
                () -> stationCommandService.uploadStationImage(STATION_ID, gif));
        verify(storageService, never()).upload(any(StorageUploadRequest.class));
        verify(stationRepository, never()).save(any(Station.class));
        verify(assetLifecycleService, never()).recordPending(any(), anyString(), any());
    }

    @Test
    void storageFailure_neverUpdatesDbPointer() {
        Station station = stationWithImage(null);
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(station));
        when(storageService.upload(any(StorageUploadRequest.class)))
                .thenThrow(new StorageWriteFailedException("S3 unavailable"));

        MockMultipartFile png = pngFile();
        assertThrows(StorageWriteFailedException.class,
                () -> stationCommandService.uploadStationImage(STATION_ID, png));

        verify(assetLifecycleService, never()).recordPending(any(), anyString(), any());
        verify(stationImagePointerService, never())
                .applyPointer(any(), any(), any(), any());
        verify(stationRepository, never()).save(any(Station.class));
    }

    @Test
    void concurrentReplace_throwsConcurrentAssetReplaceException() {
        Station station = stationWithImage("https://cdn.example.com/public/stations/old.jpg");
        when(stationRepository.findById(STATION_ID)).thenReturn(Optional.of(station));

        StorageUploadResult result = new StorageUploadResult(
                "public/stations/" + STATION_ID + "/cover/new.png",
                "https://cdn.example.com/public/stations/" + STATION_ID + "/cover/new.png",
                "image/png",
                12L,
                "abc",
                StorageVisibility.PUBLIC,
                "s3");
        when(storageService.upload(any(StorageUploadRequest.class))).thenReturn(result);

        StoredAsset pending = StoredAsset.builder()
                .id(UUID.randomUUID())
                .provider("s3")
                .objectKey(result.objectKey())
                .visibility(StorageVisibility.PUBLIC)
                .status(StoredAssetStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
        when(assetLifecycleService.recordPending(eq(result), eq("station"), eq(STATION_ID)))
                .thenReturn(pending);

        doThrow(new ConcurrentAssetReplaceException("conflict"))
                .when(stationImagePointerService)
                .applyPointer(eq(STATION_ID), eq(station.getImageUrl()), eq(result), eq(pending.getId()));

        assertThrows(ConcurrentAssetReplaceException.class,
                () -> stationCommandService.uploadStationImage(STATION_ID, pngFile()));
    }

    private static Station stationWithImage(String imageUrl) {
        return Station.builder()
                .id(STATION_ID)
                .lineId(LINE_ID)
                .code("S1")
                .name("S")
                .sortOrder(1)
                .collectorCount(0)
                .status(MetroStatus.ACTIVE)
                .scanKeyStatus(ScanKeyStatus.ACTIVE)
                .imageUrl(imageUrl)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /** Valid 1024×1024 PNG so FileValidator accepts STATION_COVER constraints. */
    private static MockMultipartFile pngFile() {
        try {
            BufferedImage image = new BufferedImage(1024, 1024, BufferedImage.TYPE_INT_RGB);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(image, "png", out);
            return new MockMultipartFile("file", "cover.png", "image/png", out.toByteArray());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
