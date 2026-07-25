package metro.ExoticStamp.modules.collection.application.support;

import metro.ExoticStamp.modules.collection.domain.exception.InvalidRequestException;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import metro.ExoticStamp.modules.collection.domain.model.CampaignType;
import metro.ExoticStamp.modules.collection.domain.model.StampDesignStatus;
import metro.ExoticStamp.modules.collection.domain.model.StampRarity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CollectionEnumParserTest {

    @Test
    void parseCampaignType_nullOrBlank_returnsNull() {
        assertNull(CollectionEnumParser.parseCampaignType(null));
        assertNull(CollectionEnumParser.parseCampaignType("  "));
    }

    @Test
    void parseCampaignType_trimsAndUppercases() {
        assertEquals(CampaignType.SEASONAL, CollectionEnumParser.parseCampaignType("  seasonal  "));
    }

    @Test
    void parseCampaignType_invalid_throws() {
        assertThrows(InvalidRequestException.class, () -> CollectionEnumParser.parseCampaignType("NOT_A_TYPE"));
    }

    @Test
    void parseCampaignStatus_validAndInvalid() {
        assertEquals(CampaignStatus.ACTIVE, CollectionEnumParser.parseCampaignStatus("active"));
        assertNull(CollectionEnumParser.parseCampaignStatus(""));
        assertThrows(InvalidRequestException.class, () -> CollectionEnumParser.parseCampaignStatus("BAD"));
    }

    @Test
    void parseRarity_validAndInvalid() {
        assertEquals(StampRarity.RARE, CollectionEnumParser.parseRarity(" rare "));
        assertNull(CollectionEnumParser.parseRarity(null));
        assertThrows(InvalidRequestException.class, () -> CollectionEnumParser.parseRarity("MYTHIC"));
    }

    @Test
    void parseStampDesignStatus_validAndInvalid() {
        assertEquals(StampDesignStatus.DRAFT, CollectionEnumParser.parseStampDesignStatus("draft"));
        assertNull(CollectionEnumParser.parseStampDesignStatus(" "));
        assertThrows(InvalidRequestException.class, () -> CollectionEnumParser.parseStampDesignStatus("UNKNOWN"));
    }

    @Test
    void parseScanType_mapsQrAliasAndRejectsUnknown() {
        assertEquals("NFC", CollectionEnumParser.parseScanType("nfc"));
        assertEquals("QR_STATIC", CollectionEnumParser.parseScanType("QR"));
        assertEquals("QR_DYNAMIC_PLACEHOLDER", CollectionEnumParser.parseScanType("QR_DYNAMIC_PLACEHOLDER"));
        assertThrows(InvalidRequestException.class, () -> CollectionEnumParser.parseScanType(null));
        assertThrows(InvalidRequestException.class, () -> CollectionEnumParser.parseScanType("BARCODE"));
    }

    @Test
    void parseScanType_blank_throwsRequired() {
        assertThrows(InvalidRequestException.class, () -> CollectionEnumParser.parseScanType("   "));
    }
}