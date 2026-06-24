package metro.ExoticStamp.infra.mail.queue;

import java.time.LocalDateTime;

public final class MailRetryPolicy {

    private MailRetryPolicy() {
    }

    /**
     * Exponential backoff: 1 min, 5 min, 30 min, 2 h, 6 h
     * retryCount is the CURRENT count (before incrementing).
     */
    public static LocalDateTime nextRetryAt(int retryCount) {
        long[] minutesBackoff = {1, 5, 30, 120, 360};
        int idx = Math.min(retryCount, minutesBackoff.length - 1);
        return LocalDateTime.now().plusMinutes(minutesBackoff[idx]);
    }
}
