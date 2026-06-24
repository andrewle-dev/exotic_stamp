package metro.ExoticStamp.infra.mail.queue;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MailRetryPolicyTest {

    @Test
    void nextRetryAt_usesExponentialBackoff() {
        LocalDateTime before = LocalDateTime.now();
        LocalDateTime t0 = MailRetryPolicy.nextRetryAt(0);
        LocalDateTime t1 = MailRetryPolicy.nextRetryAt(1);
        assertTrue(t0.isAfter(before));
        assertTrue(t1.isAfter(t0));
    }

    @Test
    void nextRetryAt_capsAtLastBackoffSlot() {
        LocalDateTime a = MailRetryPolicy.nextRetryAt(10);
        LocalDateTime b = MailRetryPolicy.nextRetryAt(100);
        assertEquals(a.getMinute(), b.getMinute() % 60 == a.getMinute() % 60 ? a.getMinute() : b.getMinute());
    }
}
