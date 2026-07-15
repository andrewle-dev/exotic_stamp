package metro.ExoticStamp.modules.mobile.application.service;

import metro.ExoticStamp.modules.mobile.application.view.MobileAppConfigView;
import metro.ExoticStamp.modules.mobile.config.MobileAppConfigProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MobileAppConfigQueryServiceTest {

    private MobileAppConfigProperties properties;
    private MobileAppConfigQueryService service;

    @BeforeEach
    void setUp() {
        properties = new MobileAppConfigProperties();
        service = new MobileAppConfigQueryService(properties);
    }

    @Test
    void getAppConfig_mapsYamlDefaults() {
        MobileAppConfigView view = service.getAppConfig();

        assertEquals("0.1.0", view.android().minimumSupportedVersion());
        assertEquals("0.1.0", view.android().latestVersion());
        assertFalse(view.android().forceUpdate());
        assertNull(view.android().storeUrl());

        assertEquals("0.1.0", view.ios().minimumSupportedVersion());
        assertEquals("0.1.0", view.ios().latestVersion());
        assertFalse(view.ios().forceUpdate());
        assertNull(view.ios().storeUrl());

        assertFalse(view.maintenance().enabled());
        assertNull(view.maintenance().message());
    }

    @Test
    void getAppConfig_mapsConfiguredValuesAndBlankToNull() {
        properties.getAndroid().setMinimumSupportedVersion("1.0.0");
        properties.getAndroid().setLatestVersion("1.2.0");
        properties.getAndroid().setForceUpdate(true);
        properties.getAndroid().setStoreUrl("https://play.google.com/store/apps/details?id=com.example");

        properties.getIos().setMinimumSupportedVersion("1.0.0");
        properties.getIos().setLatestVersion("1.1.0");
        properties.getIos().setForceUpdate(false);
        properties.getIos().setStoreUrl("  ");

        properties.getMaintenance().setEnabled(true);
        properties.getMaintenance().setMessage(" Scheduled maintenance ");

        MobileAppConfigView view = service.getAppConfig();

        assertEquals("1.0.0", view.android().minimumSupportedVersion());
        assertEquals("1.2.0", view.android().latestVersion());
        assertTrue(view.android().forceUpdate());
        assertEquals("https://play.google.com/store/apps/details?id=com.example", view.android().storeUrl());

        assertNull(view.ios().storeUrl());
        assertTrue(view.maintenance().enabled());
        assertEquals("Scheduled maintenance", view.maintenance().message());
    }
}
