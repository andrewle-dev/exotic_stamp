package metro.ExoticStamp.modules.metro.application.support;

import metro.ExoticStamp.modules.metro.domain.exception.InvalidScanPayloadException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ScanPayloadParserTest {

    private final ScanPayloadParser parser = new ScanPayloadParser();

    @Test
    void extractRawKey_bareKey() {
        assertEquals("nfc_test_home_001", parser.extractRawKey("nfc_test_home_001"));
    }

    @Test
    void extractRawKey_uri() {
        assertEquals("nfc_test_home_001",
                parser.extractRawKey("metrostamp://scan?k=nfc_test_home_001"));
    }

    @Test
    void extractRawKey_blank_throws() {
        assertThrows(InvalidScanPayloadException.class, () -> parser.extractRawKey("  "));
    }

    @Test
    void extractRawKey_uriMissingK_throws() {
        assertThrows(InvalidScanPayloadException.class,
                () -> parser.extractRawKey("metrostamp://scan?x=1"));
    }

    @Test
    void buildPayloadToWrite() {
        assertEquals("metrostamp://scan?k=nfc_abc", parser.buildPayloadToWrite("nfc_abc"));
    }
}
