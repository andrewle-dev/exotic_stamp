package metro.ExoticStamp.infra.storage;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

    /**
     * Upload a file and return its public-accessible URL.
     *
     * @param file   the multipart file to upload
     * @param folder logical folder/prefix, e.g. "metro/stations/42"
     * @return full URL to access the file
     */
    String upload(MultipartFile file, String folder);

    /**
     * Delete a file by its URL or storage key.
     * Implementations must handle "file not found" gracefully (log, no throw).
     */
    void delete(String fileUrl);
}
