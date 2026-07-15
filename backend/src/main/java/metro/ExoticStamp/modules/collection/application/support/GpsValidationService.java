package metro.ExoticStamp.modules.collection.application.support;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.collection.application.view.ResolvedStationView;
import metro.ExoticStamp.modules.collection.config.CollectionProperties;
import metro.ExoticStamp.modules.collection.domain.policy.CollectionGpsPolicy;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Application adapter around {@link CollectionGpsPolicy}: maps views/config into domain inputs.
 */
@Component
@RequiredArgsConstructor
public class GpsValidationService {

    private final CollectionProperties collectionProperties;

    public GpsValidationResult validate(
            BigDecimal latitude,
            BigDecimal longitude,
            BigDecimal accuracyMeters,
            ResolvedStationView station
    ) {
        CollectionProperties.Gps gps = collectionProperties.getGps();
        double earthRadius = gps != null && gps.getEarthRadiusMeters() > 0
                ? gps.getEarthRadiusMeters()
                : CollectionGpsPolicy.DEFAULT_EARTH_RADIUS_METERS;

        CollectionGpsPolicy.StationLocation location = new CollectionGpsPolicy.StationLocation(
                station != null ? station.latitude() : null,
                station != null ? station.longitude() : null,
                station != null ? station.zoneRadiusMeters() : null
        );

        CollectionGpsPolicy.GpsCheckResult checked = CollectionGpsPolicy.validate(
                latitude, longitude, accuracyMeters, location, earthRadius);

        return GpsValidationResult.builder()
                .distanceMeters(checked.distanceMeters())
                .accuracyMeters(checked.accuracyMeters())
                .verified(checked.verified())
                .build();
    }

    @Builder
    public record GpsValidationResult(
            BigDecimal distanceMeters,
            BigDecimal accuracyMeters,
            boolean verified
    ) {
    }
}
