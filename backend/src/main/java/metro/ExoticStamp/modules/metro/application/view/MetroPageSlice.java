package metro.ExoticStamp.modules.metro.application.view;

import java.util.List;

public record MetroPageSlice<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {
}
