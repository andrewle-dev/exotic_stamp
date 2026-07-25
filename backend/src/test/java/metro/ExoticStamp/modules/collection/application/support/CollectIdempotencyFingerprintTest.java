package metro.ExoticStamp.modules.collection.application.support;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CollectIdempotencyFingerprintTest {

    @Test
    void sameInputs_sameHash_caseInsensitiveScanType() {
        UUID u = UUID.randomUUID();
        UUID s = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        String a = CollectIdempotencyFingerprint.compute(u, s, c, "nfc");
        String b = CollectIdempotencyFingerprint.compute(u, s, c, "NFC");
        assertEquals(64, a.length());
        assertEquals(a, b);
        assertTrue(CollectIdempotencyFingerprint.matches(a, b));
    }

    @Test
    void differentCampaign_differentHash() {
        UUID u = UUID.randomUUID();
        UUID s = UUID.randomUUID();
        String a = CollectIdempotencyFingerprint.compute(u, s, UUID.randomUUID(), "NFC");
        String b = CollectIdempotencyFingerprint.compute(u, s, UUID.randomUUID(), "NFC");
        assertNotEquals(a, b);
    }

    @Test
    void compute_rejectsNullInputs() {
        UUID u = UUID.randomUUID();
        UUID s = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        assertThrows(IllegalArgumentException.class,
                () -> CollectIdempotencyFingerprint.compute(null, s, c, "NFC"));
        assertThrows(IllegalArgumentException.class,
                () -> CollectIdempotencyFingerprint.compute(u, null, c, "NFC"));
        assertThrows(IllegalArgumentException.class,
                () -> CollectIdempotencyFingerprint.compute(u, s, null, "NFC"));
    }

    @Test
    void matches_nullOrBlank_returnsFalse() {
        String fp = CollectIdempotencyFingerprint.compute(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "NFC");
        assertFalse(CollectIdempotencyFingerprint.matches(null, fp));
        assertFalse(CollectIdempotencyFingerprint.matches(fp, null));
    }

    @Test
    void nullScanType_treatedAsEmptyInCanonical() {
        UUID u = UUID.randomUUID();
        UUID s = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        String withNull = CollectIdempotencyFingerprint.compute(u, s, c, null);
        String withBlank = CollectIdempotencyFingerprint.compute(u, s, c, "   ");
        assertEquals(withNull, withBlank);
    }
}
