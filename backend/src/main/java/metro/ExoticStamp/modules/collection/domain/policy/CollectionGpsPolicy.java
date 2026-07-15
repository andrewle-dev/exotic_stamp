package metro.ExoticStamp.modules.collection.domain.policy;

import metro.ExoticStamp.common.utils.GeoDistance;
import metro.ExoticStamp.modules.collection.domain.exception.GpsAccuracyTooLowException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsInvalidException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsOutOfRangeException;
import metro.ExoticStamp.modules.collection.domain.exception.GpsRequiredException;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * GPS eligibility rules for stamp collection.
 * Application supplies coordinates and configured earth radius; no application imports.
 */
public final class CollectionGpsPolicy {

    public static final int MIN_ZONE_RADIUS = 20;
    public static final int MAX_ZONE_RADIUS = 1000;
    public static final double DEFAULT_RADIUS_METERS = 150.0;
    public static final double MAX_ACCURACY_METERS = 200.0;
    public static final double DEFAULT_EARTH_RADIUS_METERS = 6_371_000.0;

    private CollectionGpsPolicy() {
    }

    public record StationLocation(
            BigDecimal latitude,
            BigDecimal longitude,
            Integer zoneRadiusMeters
    ) {
    }

    public record GpsCheckResult(
            BigDecimal distanceMeters,
            BigDecimal accuracyMeters,
            boolean verified
    ) {
    }

    public static GpsCheckResult validate(
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal accuracyMeters,
            StationLocation station,
            double earthRadiusMeters
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
        if (station == null || station.latitude() == null || station.longitude() == null) {
            throw new GpsInvalidException("Station coordinates are not configured");
        }

        double radius = earthRadiusMeters > 0 ? earthRadiusMeters : DEFAULT_EARTH_RADIUS_METERS;
        double distanceMeters = GeoDistance.metersBetween(
                lat,
                lng,
                station.latitude().doubleValue(),
                station.longitude().doubleValue(),
                radius);

        double allowedRadius = resolveZoneRadius(station.zoneRadiusMeters());
        if (distanceMeters > allowedRadius) {
            throw new GpsOutOfRangeException(distanceMeters, allowedRadius);
        }

        return new GpsCheckResult(round(distanceMeters), round(accuracy), true);
    }

    public static double resolveZoneRadius(Integer zoneRadiusMeters) {
        if (zoneRadiusMeters != null
                && zoneRadiusMeters >= MIN_ZONE_RADIUS
                && zoneRadiusMeters <= MAX_ZONE_RADIUS) {
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
}
