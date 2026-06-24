package metro.ExoticStamp.modules.collection.application.support;

import metro.ExoticStamp.modules.collection.application.view.ResolvedStationView;
import metro.ExoticStamp.modules.collection.config.CollectionProperties;
import metro.ExoticStamp.modules.collection.domain.exception.GpsAccuracyTooLowException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsInvalidException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsOutOfRangeException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsRequiredException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GpsValidationServiceTest {

    private GpsValidationService service;

    @BeforeEach
    void setUp() {
        CollectionProperties props = new CollectionProperties();
        CollectionProperties.Gps gps = new CollectionProperties.Gps();
        gps.setEarthRadiusMeters(6371000);
        service = new GpsValidationService(props);
    }

    @Test
    void missingCoords_throwsGpsRequired() {
        ResolvedStationView station = station(10.0, 20.0, 150);
        assertThrows(GpsRequiredException.class, () ->
                service.validate(null, BigDecimal.ONE, BigDecimal.valueOf(30), station));
    }

    @Test
    void invalidRange_throwsGpsInvalid() {
        ResolvedStationView station = station(10.0, 20.0, 150);
        assertThrows(GpsInvalidException.class, () ->
                service.validate(BigDecimal.valueOf(95), BigDecimal.ZERO, BigDecimal.valueOf(30), station));
    }

    @Test
    void accuracyTooLow_throws() {
        ResolvedStationView station = station(10.0, 20.0, 150);
        assertThrows(GpsAccuracyTooLowException.class, () ->
                service.validate(BigDecimal.valueOf(10), BigDecimal.valueOf(20), BigDecimal.valueOf(250), station));
    }

    @Test
    void withinRadius_succeeds() {
        ResolvedStationView station = station(10.0, 20.0, 150);
        GpsValidationService.GpsValidationResult result = service.validate(
                BigDecimal.valueOf(10), BigDecimal.valueOf(20), BigDecimal.valueOf(35), station);
        assertTrue(result.verified());
        assertEquals(0, result.distanceMeters().compareTo(BigDecimal.ZERO.setScale(2)));
    }

    @Test
    void outsideRadius_throwsOutOfRange() {
        ResolvedStationView station = station(10.0, 20.0, 50);
        assertThrows(GpsOutOfRangeException.class, () ->
                service.validate(BigDecimal.valueOf(10.01), BigDecimal.valueOf(20.01), BigDecimal.valueOf(30), station));
    }

    @Test
    void fallbackRadius150_whenZoneInvalid() {
        assertEquals(150.0, GpsValidationService.resolveZoneRadius(5));
        assertEquals(150.0, GpsValidationService.resolveZoneRadius(5000));
        assertEquals(80.0, GpsValidationService.resolveZoneRadius(80));
    }

    private static ResolvedStationView station(double lat, double lng, int zone) {
        return ResolvedStationView.builder()
                .latitude(BigDecimal.valueOf(lat))
                .longitude(BigDecimal.valueOf(lng))
                .zoneRadiusMeters(zone)
                .build();
    }
}
