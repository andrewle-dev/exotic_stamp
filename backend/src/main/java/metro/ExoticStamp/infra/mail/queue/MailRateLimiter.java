package metro.ExoticStamp.infra.mail.queue;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MailRateLimiter {

    private final int maxPerMinute;
    private final AtomicInteger counter = new AtomicInteger(0);
    private final AtomicLong windowStartEpochSecond = new AtomicLong(0);

    public MailRateLimiter(
            @Value("${application.mail.queue.max-per-minute:30}") int maxPerMinute) {
        this.maxPerMinute = maxPerMinute;
    }

    public boolean tryAcquire() {
        long now = Instant.now().getEpochSecond();
        long windowStart = windowStartEpochSecond.get();
        if (now - windowStart >= 60) {
            windowStartEpochSecond.set(now);
            counter.set(0);
        }
        return counter.incrementAndGet() <= maxPerMinute;
    }
}
