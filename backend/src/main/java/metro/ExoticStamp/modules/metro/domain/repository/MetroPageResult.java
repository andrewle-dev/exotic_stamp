package metro.ExoticStamp.modules.metro.domain.repository;

import java.util.List;

public record MetroPageResult<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {
}
