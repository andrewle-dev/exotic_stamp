package metro.ExoticStamp.modules.community.application.support;

import metro.ExoticStamp.modules.community.domain.exception.SharePlatformInvalidException;
import metro.ExoticStamp.modules.community.domain.exception.ShareTypeInvalidException;
import metro.ExoticStamp.modules.community.domain.model.SharePlatform;
import metro.ExoticStamp.modules.community.domain.model.ShareType;

public final class CommunityEnumParser {

    private CommunityEnumParser() {
    }

    public static SharePlatform parseSharePlatform(String value) {
        if (value == null || value.isBlank()) {
            throw new SharePlatformInvalidException("Platform is required");
        }
        try {
            return SharePlatform.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new SharePlatformInvalidException("Invalid share platform: " + value);
        }
    }

    public static ShareType parseShareType(String value) {
        if (value == null || value.isBlank()) {
            throw new ShareTypeInvalidException("Share type is required");
        }
        try {
            return ShareType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new ShareTypeInvalidException("Invalid share type: " + value);
        }
    }
}
