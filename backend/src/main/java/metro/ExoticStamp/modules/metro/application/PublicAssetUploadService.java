package metro.ExoticStamp.modules.metro.application;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import metro.ExoticStamp.infra.storage.AssetUploadPurpose;
import metro.ExoticStamp.infra.storage.AssetUploadPurposeMapper;
import metro.ExoticStamp.infra.storage.FileValidator;
import metro.ExoticStamp.infra.storage.StorageService;
import metro.ExoticStamp.infra.storage.StorageUploadRequest;
import metro.ExoticStamp.infra.storage.StorageUploadResult;
import metro.ExoticStamp.infra.storage.StorageVisibility;
import metro.ExoticStamp.infra.storage.asset.AssetLifecycleService;
import metro.ExoticStamp.infra.storage.asset.StoredAsset;
import metro.ExoticStamp.modules.metro.application.support.MetroAuditHelper;
import metro.ExoticStamp.modules.metro.application.view.PublicAssetUploadView;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class PublicAssetUploadService {

    private final FileValidator fileValidator;
    private final StorageService storageService;
    private final AssetLifecycleService assetLifecycleService;
    private final MetroAuditHelper metroAuditHelper;

    @Transactional
    public PublicAssetUploadView uploadPublicAsset(MultipartFile file) {
        return uploadPublicAsset(file, AssetUploadPurpose.GENERIC);
    }

    public PublicAssetUploadView uploadPublicAsset(MultipartFile file, AssetUploadPurpose purpose) {
        if (file == null) {
            throw new InvalidFileException("File is required");
        }
        AssetUploadPurpose resolved = purpose == null ? AssetUploadPurpose.GENERIC : purpose;
        FileValidator.DetectedUpload detected = fileValidator.validateAndDetect(file, resolved);

        StorageUploadRequest request = StorageUploadRequest.of(
                file,
                AssetUploadPurposeMapper.toCategory(resolved),
                StorageVisibility.PUBLIC,
                (String) null,
                detected.contentType(),
                detected.extension()
        );
        StorageUploadResult result = storageService.upload(request);
        StoredAsset pending = assetLifecycleService.recordPending(result, "public_upload", null);
        assetLifecycleService.activate(pending.getId());
        metroAuditHelper.schedulePublicAssetUploaded(result.publicUrl());
        return PublicAssetUploadView.builder().url(result.publicUrl()).build();
    }
}
