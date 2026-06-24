package metro.ExoticStamp.infra.storage;

import metro.ExoticStamp.common.exceptions.storage.FileTooLargeException;
import metro.ExoticStamp.common.exceptions.storage.InvalidFileException;
import metro.ExoticStamp.common.exceptions.storage.InvalidImageTypeException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Component
public class FileValidator {

    private final StorageProperties storageProperties;

    public FileValidator(StorageProperties storageProperties) {
        this.storageProperties = storageProperties;
    }

    public void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is required");
        }
        String contentType = file.getContentType();
        if (contentType == null || contentType.isBlank()) {
            throw new InvalidImageTypeException("Content type is missing");
        }
        if (!storageProperties.getFile().getAllowedTypes().contains(contentType)) {
            throw new InvalidImageTypeException("Unsupported image type: " + contentType);
        }
        long maxBytes = storageProperties.getFile().getMaxSizeMb() * 1024L * 1024L;
        if (file.getSize() > maxBytes) {
            throw new FileTooLargeException(
                    "File exceeds maximum size of " + storageProperties.getFile().getMaxSizeMb() + " MB");
        }
    }
}
