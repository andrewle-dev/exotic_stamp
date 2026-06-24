package metro.ExoticStamp.modules.metro.application.support;

import metro.ExoticStamp.modules.metro.domain.model.MetroStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanKeyStatus;
import metro.ExoticStamp.modules.metro.domain.model.ScanType;

public final class MetroEnumParser {

    private MetroEnumParser() {
    }

    public static MetroStatus parseStatus(String value) {
        return value == null || value.isBlank() ? null : MetroStatus.valueOf(value);
    }

    public static ScanKeyStatus parseScanKeyStatus(String value) {
        return value == null || value.isBlank() ? null : ScanKeyStatus.valueOf(value);
    }

    public static ScanType parseScanType(String value) {
        return value == null || value.isBlank() ? null : ScanType.valueOf(value);
    }
}
