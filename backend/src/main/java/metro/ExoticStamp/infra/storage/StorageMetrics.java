package metro.ExoticStamp.infra.storage;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class StorageMetrics {

    private final Counter uploadSuccess;
    private final Counter uploadFailure;
    private final Counter orphanCreated;
    private final Counter cleanupSuccess;
    private final Counter cleanupFailure;
    private final Counter missingReferenced;

    public StorageMetrics(MeterRegistry registry) {
        this.uploadSuccess = Counter.builder("storage.upload.success").register(registry);
        this.uploadFailure = Counter.builder("storage.upload.failure").register(registry);
        this.orphanCreated = Counter.builder("storage.orphan.created").register(registry);
        this.cleanupSuccess = Counter.builder("storage.cleanup.success").register(registry);
        this.cleanupFailure = Counter.builder("storage.cleanup.failure").register(registry);
        this.missingReferenced = Counter.builder("storage.referenced.missing").register(registry);
    }

    public void recordUploadSuccess() {
        uploadSuccess.increment();
    }

    public void recordUploadFailure() {
        uploadFailure.increment();
    }

    public void recordOrphanCreated() {
        orphanCreated.increment();
    }

    public void recordCleanupSuccess() {
        cleanupSuccess.increment();
    }

    public void recordCleanupFailure() {
        cleanupFailure.increment();
    }

    public void recordMissingReferenced() {
        missingReferenced.increment();
    }
}
