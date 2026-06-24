package metro.ExoticStamp.modules.collection.application.support;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.utils.GeoDistance;
import metro.ExoticStamp.modules.collection.application.view.ResolvedStationView;
import metro.ExoticStamp.modules.collection.config.CollectionProperties;
import metro.ExoticStamp.modules.collection.domain.exception.GpsAccuracyTooLowException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsInvalidException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsOutOfRangeException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsRequiredException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
@RequiredArgsConstructor
public class GpsValidationService {

    private static final int MIN_ZONE_RADIUS = 20;
    private static final int MAX_ZONE_RADIUS = 1000;
    private static final double DEFAULT_RADIUS_METERS = 150.0;
    private static final double MAX_ACCURACY_METERS = 200.0;

    private final CollectionProperties collectionProperties;

    public GpsValidationResult validate(
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal accuracyMeters,
            ResolvedStationView station
    ) {
        if (latitude == null || longitude == null) {
            throw new GpsRequiredException("GPS latitude and longitude are required");
        }
        if (accuracyMeters == null) {
            throw new GpsRequiredException("GPS accuracyMeters is required");
        }
        double lat = latitude.doubleValue();
        double lng = longitude.doubleValue();
        double accuracy = accuracyMeters.doubleValue();

        if (!isValidCoordinate(lat, lng)) {
            throw new GpsInvalidException("GPS coordinates are out of valid range");
        }
        if (accuracy <= 0 || accuracy > MAX_ACCURACY_METERS) {
            throw new GpsAccuracyTooLowException(accuracy, MAX_ACCURACY_METERS);
        }
        if (station.latitude() == null || station.longitude() == null) {
            throw new GpsInvalidException("Station coordinates are not configured");
        }

        CollectionProperties.Gps gps = collectionProperties.getGps();
        double earthRadius = gps != null && gps.getEarthRadiusMeters() > 0
                ? gps.getEarthRadiusMeters()
                : 6371000.0;

        double distanceMeters = GeoDistance.metersBetween(
                lat,
                lng,
                station.latitude().doubleValue(),
                station.longitude().doubleValue(),
                earthRadius);

        double allowedRadius = resolveZoneRadius(station.zoneRadiusMeters());
        if (distanceMeters > allowedRadius) {
            throw new GpsOutOfRangeException(distanceMeters, allowedRadius);
        }

        return GpsValidationResult.builder()
                .distanceMeters(round(distanceMeters))
                .accuracyMeters(round(accuracy))
                .verified(true)
                .build();
    }

    static double resolveZoneRadius(Integer zoneRadiusMeters) {
        if (zoneRadiusMeters != null && zoneRadiusMeters >= MIN_ZONE_RADIUS && zoneRadiusMeters <= MAX_ZONE_RADIUS) {
            return zoneRadiusMeters;
        }
        return DEFAULT_RADIUS_METERS;
    }

    private static boolean isValidCoordinate(double lat, double lng) {
        return lat >= -90 && lat <= 90 && lng >= -180 && lng <= 180;
    }

    private static BigDecimal round(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }

    @Builder
    public record GpsValidationResult(
            BigDecimal distanceMeters,
            BigDecimal accuracyMeters,
            boolean verified
    ) {}
}
