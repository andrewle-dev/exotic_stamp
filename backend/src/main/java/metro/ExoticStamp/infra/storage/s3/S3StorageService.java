package metro.ExoticStamp.infra.storage.s3;

import metro.ExoticStamp.infra.storage.StorageService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@ConditionalOnProperty(name = "storage.provider", havingValue = "s3")
public class S3StorageService implements StorageService {

    @Override
    public String upload(MultipartFile file, String folder) {
        throw new UnsupportedOperationException("S3 storage not configured yet");
    }

    @Override
    public void delete(String fileUrl) {
        throw new UnsupportedOperationException("S3 storage not configured yet");
    }
}
