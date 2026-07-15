package metro.ExoticStamp.modules.metro.application.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScanKeyRedactorTest {

    @Test
    void redact_rawKey_masksAfterPrefix() {
        String redacted = ScanKeyRedactor.redact("nfc_abcdefghij");
        assertEquals("nfc_abcd****", redacted);
    }

    @Test
    void redact_uri_masksKeyOnly() {
        String redacted = ScanKeyRedactor.redact("metrostamp://scan?k=nfc_abcdefghij");
        assertEquals("metrostamp://scan?k=nfc_abcd****", redacted);
        assertTrue(!redacted.contains("efghij"));
    }

    @Test
    void redact_null() {
        assertNull(ScanKeyRedactor.redact(null));
    }
}
