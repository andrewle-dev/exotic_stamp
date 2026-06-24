package metro.ExoticStamp.common.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoDistanceTest {

    @Test
    void samePoint_zeroMeters() {
        double d = GeoDistance.metersBetween(10.0, 20.0, 10.0, 20.0, 6371000);
        assertEquals(0.0, d, 0.01);
    }

    @Test
    void shortOffset_positiveDistance() {
        double d = GeoDistance.metersBetween(0, 0, 0.001, 0, 6371000);
        assertTrue(d > 100 && d < 120);
    }
}
