package metro.ExoticStamp.modules.metro.infrastructure.seeder;

import metro.ExoticStamp.modules.collection.infrastructure.bootstrap.CollectionBootstrapper;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Profile;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DevOnlySeederProfileTest {

    @Test
    void metroLineSeeder_isDevProfileOnly() {
        Profile profile = MetroLineSeeder.class.getAnnotation(Profile.class);
        assertNotNull(profile);
        assertArrayEquals(new String[]{"dev"}, profile.value());
    }

    @Test
    void collectionBootstrapper_isDevProfileOnly() {
        Profile profile = CollectionBootstrapper.class.getAnnotation(Profile.class);
        assertNotNull(profile);
        assertArrayEquals(new String[]{"dev"}, profile.value());
    }
}
