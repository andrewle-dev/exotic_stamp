package metro.ExoticStamp.infra.security.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimitKeyHasherTest {

    private RateLimitKeyHasher hasher;

    @BeforeEach
    void setUp() {
        RateLimitProperties props = new RateLimitProperties();
        props.setKeyPepper("unit-test-pepper-value-32chars!!");
        hasher = new RateLimitKeyHasher(props);
    }

    @Test
    void hmacHex_isLowercaseHex32Chars() {
        String hex = hasher.hmacHex("user@example.com");
        assertEquals(32, hex.length());
        assertTrue(hex.matches("[0-9a-f]{32}"));
    }

    @Test
    void hmacHex_doesNotContainRawEmail() {
        String email = "Secret.User+tag@Example.COM";
        String hex = hasher.hmacHex(hasher.normalizeEmail(email));
        assertFalse(hex.contains("secret"));
        assertFalse(hex.contains("example"));
        assertFalse(hex.toLowerCase().contains("user"));
        assertFalse(hex.contains("@"));
    }

    @Test
    void normalizeEmail_trimsAndLowercases() {
        assertEquals("a@b.com", hasher.normalizeEmail("  A@B.Com "));
    }

    @Test
    void normalizePhone_stripsSpacesAndDashes() {
        assertEquals("+84123456789", hasher.normalizePhone(" +84 123-456-789 "));
    }

    @Test
    void fingerprintScanKey_neverEqualsRaw() {
        String raw = "nfc_test_home_001";
        String fp = hasher.fingerprintScanKey(raw);
        assertNotEquals(raw, fp);
        assertFalse(fp.contains("nfc_"));
        assertEquals(32, fp.length());
    }

    @Test
    void buildKey_joinsHashedParts_withoutRawPii() {
        String emailHash = hasher.hmacHex(hasher.normalizeEmail("a@b.com"));
        String key = hasher.buildKey(RateLimitPolicyName.LOGIN, "10.0.0.1", emailHash);
        assertTrue(key.startsWith("rl:v1:login:10.0.0.1:"));
        assertFalse(key.contains("a@b.com"));
        assertTrue(key.contains(emailHash));
    }

    @Test
    void buildKey_omitsBlankParts() {
        String key = hasher.buildKey(RateLimitPolicyName.REFRESH, "127.0.0.1", null, "  ");
        assertEquals("rl:v1:refresh:127.0.0.1", key);
    }

    @Test
    void hmacHex_blankPepper_throws() {
        RateLimitProperties props = new RateLimitProperties();
        props.setKeyPepper("  ");
        RateLimitKeyHasher broken = new RateLimitKeyHasher(props);
        assertThrows(IllegalStateException.class, () -> broken.hmacHex("x"));
    }

    @Test
    void sameInput_samePepper_isDeterministic() {
        assertEquals(hasher.hmacHex("same"), hasher.hmacHex("same"));
    }
}
