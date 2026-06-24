package metro.ExoticStamp.modules.collection.application.support;

import metro.ExoticStamp.modules.collection.domain.exception.InvalidRequestException;
import metro.ExoticStamp.modules.collection.domain.model.CampaignStatus;
import metro.ExoticStamp.modules.collection.domain.model.CampaignType;
import metro.ExoticStamp.modules.collection.domain.model.StampDesignStatus;
import metro.ExoticStamp.modules.collection.domain.model.StampRarity;

public final class CollectionEnumParser {

    private CollectionEnumParser() {}

    public static CampaignType parseCampaignType(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return CampaignType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Invalid campaignType: " + value);
        }
    }

    public static CampaignStatus parseCampaignStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return CampaignStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Invalid campaign status: " + value);
        }
    }

    public static StampRarity parseRarity(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return StampRarity.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Invalid rarity: " + value);
        }
    }

    public static StampDesignStatus parseStampDesignStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return StampDesignStatus.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new InvalidRequestException("Invalid stamp design status: " + value);
        }
    }

    public static String parseScanType(String value) {
        if (value == null || value.isBlank()) {
            throw new InvalidRequestException("scanType is required");
        }
        String normalized = value.trim().toUpperCase();
        return switch (normalized) {
            case "NFC", "QR_STATIC", "QR_DYNAMIC_PLACEHOLDER" -> normalized;
            case "QR" -> "QR_STATIC";
            default -> throw new InvalidRequestException("Invalid scanType: " + value);
        };
    }
}
