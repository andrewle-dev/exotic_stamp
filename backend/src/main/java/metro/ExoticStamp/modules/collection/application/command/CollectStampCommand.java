package metro.ExoticStamp.modules.collection.application.command;

import java.math.BigDecimal;
import java.util.UUID;

public record CollectStampCommand(
        UUID userId,
        UUID idempotencyKey,
        String scanType,
        String payload,
        BigDecimal latitude,
        BigDecimal longitude,
        BigDecimal accuracyMeters,
        String devicePlatform,
        String appVersion,
        String deviceFingerprint
) {
    public static CollectStampCommand legacyNfc(
            UUID userId,
            UUID idempotencyKey,
            String nfcTagId,
            String deviceFingerprint,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        return new CollectStampCommand(
                userId,
                idempotencyKey,
                "NFC",
                nfcTagId,
                latitude,
                longitude,
                null,
                null,
                null,
                deviceFingerprint
        );
    }

    public static CollectStampCommand legacyQr(
            UUID userId,
            UUID idempotencyKey,
            String qrToken,
            String deviceFingerprint,
            BigDecimal latitude,
            BigDecimal longitude
    ) {
        return new CollectStampCommand(
                userId,
                idempotencyKey,
                "QR_STATIC",
                qrToken,
                latitude,
                longitude,
                null,
                null,
                null,
                deviceFingerprint
        );
    }
}
