package metro.ExoticStamp.infra.storage.s3;

import metro.ExoticStamp.common.exceptions.storage.StorageWriteFailedException;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

final class S3ExceptionMapper {

    private S3ExceptionMapper() {
    }

    static RuntimeException mapUploadFailure(Throwable error) {
        if (error instanceof NoSuchBucketException) {
            return new StorageWriteFailedException("Storage bucket is not available", error);
        }
        if (error instanceof S3Exception s3 && s3.statusCode() == 403) {
            return new StorageWriteFailedException("Storage credentials or permissions are invalid", error);
        }
        if (error instanceof AwsServiceException || error instanceof SdkClientException) {
            return new StorageWriteFailedException("Storage write failed", error);
        }
        if (error instanceof RuntimeException runtime) {
            return runtime;
        }
        return new StorageWriteFailedException("Storage write failed", error);
    }

    static boolean isNotFound(Throwable error) {
        return error instanceof NoSuchKeyException
                || (error instanceof S3Exception s3 && s3.statusCode() == 404);
    }
}
