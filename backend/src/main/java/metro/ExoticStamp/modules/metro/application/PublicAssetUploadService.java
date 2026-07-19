package metro.ExoticStamp.modules.metro.application;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import metro.ExoticStamp.infra.storage.AssetUploadPurpose;
import metro.ExoticStamp.infra.storage.FileValidator;
import metro.ExoticStamp.infra.storage.StorageService;
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
    private final MetroAuditHelper metroAuditHelper;

    @Transactional
    public PublicAssetUploadView uploadPublicAsset(MultipartFile file) {
        return uploadPublicAsset(file, AssetUploadPurpose.GENERIC);
    }

    @Transactional
    public PublicAssetUploadView uploadPublicAsset(MultipartFile file, AssetUploadPurpose purpose) {
        if (file == null) {
            throw new InvalidFileException("File is required");
        }
        AssetUploadPurpose resolved = purpose == null ? AssetUploadPurpose.GENERIC : purpose;
        fileValidator.validate(file, resolved);
        String url = storageService.upload(file, "public");
        metroAuditHelper.schedulePublicAssetUploaded(url);
        return PublicAssetUploadView.builder().url(url).build();
    }
}
