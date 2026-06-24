package metro.ExoticStamp.common.utils;

/**
 * Haversine distance between two WGS-84 points. Earth radius is supplied by caller (typically from configuration).
 */
public final class GeoDistance {

    private GeoDistance() {
    }

    /**
     * @return great-circle distance in meters
     */
    public static double metersBetween(
            double lat1Deg,
            double lon1Deg,
            double lat2Deg,
            double lon2Deg,
            double earthRadiusMeters
    ) {
        double phi1 = Math.toRadians(lat1Deg);
        double phi2 = Math.toRadians(lat2Deg);
        double dPhi = Math.toRadians(lat2Deg - lat1Deg);
        double dLambda = Math.toRadians(lon2Deg - lon1Deg);

        double a = Math.sin(dPhi / 2) * Math.sin(dPhi / 2)
                + Math.cos(phi1) * Math.cos(phi2) * Math.sin(dLambda / 2) * Math.sin(dLambda / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return earthRadiusMeters * c;
    }
}
