package metro.ExoticStamp.modules.collection.application.port;

import metro.ExoticStamp.modules.collection.application.view.ResolvedStationView;

public interface StationScanResolverPort {

    ResolvedStationView resolve(String scanType, String payload);
}
