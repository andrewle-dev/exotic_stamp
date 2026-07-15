package metro.ExoticStamp.modules.metro.application.support;

import metro.ExoticStamp.modules.metro.domain.exception.InvalidScanPayloadException;
import metro.ExoticStamp.modules.metro.domain.model.StationScanKey;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Component
public class ScanPayloadParser {

    public static final String QUERY_KEY_PARAM = "k";

    /**
     * Extracts the raw scan key from either a bare key or a metrostamp URI.
     * Accepts:
     * <ul>
     *   <li>{@code nfc_test_home_001}</li>
     *   <li>{@code metrostamp://scan?k=nfc_test_home_001}</li>
     * </ul>
     */
    public String extractRawKey(String payload) {
        if (payload == null) {
            throw new InvalidScanPayloadException("payload is required");
        }
        String trimmed = payload.trim();
        if (trimmed.isEmpty()) {
            throw new InvalidScanPayloadException("payload must not be blank");
        }

        if (looksLikeUri(trimmed)) {
            return extractKeyFromUri(trimmed);
        }
        return trimmed;
    }

    public String buildPayloadToWrite(String rawKey) {
        return buildPayloadToWrite(StationScanKey.DEFAULT_PAYLOAD_SCHEME, rawKey);
    }

    public String buildPayloadToWrite(String payloadScheme, String rawKey) {
        String scheme = payloadScheme == null || payloadScheme.isBlank()
                ? StationScanKey.DEFAULT_PAYLOAD_SCHEME
                : payloadScheme.trim();
        return scheme + "?" + QUERY_KEY_PARAM + "=" + rawKey;
    }

    private static boolean looksLikeUri(String value) {
        return value.contains("://");
    }

    private static String extractKeyFromUri(String uriValue) {
        try {
            URI uri = new URI(uriValue);
            String query = uri.getRawQuery();
            if (query == null || query.isBlank()) {
                throw new InvalidScanPayloadException("scan payload URI must include k query parameter");
            }
            for (String part : query.split("&")) {
                int eq = part.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String name = URLDecoder.decode(part.substring(0, eq), StandardCharsets.UTF_8);
                if (QUERY_KEY_PARAM.equals(name)) {
                    String value = URLDecoder.decode(part.substring(eq + 1), StandardCharsets.UTF_8).trim();
                    if (value.isEmpty()) {
                        throw new InvalidScanPayloadException("scan payload key must not be blank");
                    }
                    return value;
                }
            }
            throw new InvalidScanPayloadException("scan payload URI must include k query parameter");
        } catch (URISyntaxException e) {
            throw new InvalidScanPayloadException("scan payload URI is malformed");
        }
    }
}
