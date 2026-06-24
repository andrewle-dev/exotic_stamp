package metro.ExoticStamp.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

import static org.junit.jupiter.api.Assertions.*;

class AdminSeedBootstrapTest {

    @Test
    void adminSeed_isDevProfileOnly() {
        Profile profile = AdminSeedBootstrap.class.getAnnotation(Profile.class);
        assertNotNull(profile);
        assertEquals("dev", profile.value()[0]);
    }
}
