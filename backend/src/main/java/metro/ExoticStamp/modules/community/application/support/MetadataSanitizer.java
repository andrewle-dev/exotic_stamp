package metro.ExoticStamp.modules.community.application.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import metro.ExoticStamp.modules.community.config.CommunityProperties;
import metro.ExoticStamp.modules.community.domain.model.SharePlatform;
import metro.ExoticStamp.modules.community.domain.model.ShareType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Component
public class MetadataSanitizer {

    private static final Set<String> BLOCKED_KEYS = Set.of(
            "password", "token", "otp", "voucher", "vouchercode", "secret", "authorization", "apikey"
    );

    private final ObjectMapper objectMapper;
    private final CommunityProperties communityProperties;

    public MetadataSanitizer(ObjectMapper objectMapper, CommunityProperties communityProperties) {
        this.objectMapper = objectMapper;
        this.communityProperties = communityProperties;
    }

    public Map<String, Object> sanitize(Map<String, Object> raw) {
        if (raw == null || raw.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> cleaned = new HashMap<>();
        for (Map.Entry<String, Object> entry : raw.entrySet()) {
            String key = entry.getKey();
            if (key == null) {
                continue;
            }
            String normalizedKey = key.trim().toLowerCase(Locale.ROOT);
            if (BLOCKED_KEYS.contains(normalizedKey)) {
                continue;
            }
            Object value = entry.getValue();
            if (value instanceof String s && s.length() > 500) {
                cleaned.put(key, s.substring(0, 500));
            } else {
                cleaned.put(key, value);
            }
        }
        enforceSizeLimit(cleaned);
        return Map.copyOf(cleaned);
    }

    private void enforceSizeLimit(Map<String, Object> metadata) {
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(metadata);
            if (bytes.length <= communityProperties.getMaxMetadataBytes()) {
                return;
            }
            metadata.clear();
            metadata.put("truncated", true);
        } catch (JsonProcessingException e) {
            metadata.clear();
        }
    }
}
