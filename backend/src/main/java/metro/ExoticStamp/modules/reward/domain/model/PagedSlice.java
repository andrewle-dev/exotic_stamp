package metro.ExoticStamp.modules.reward.domain.model;

import java.util.List;

public record PagedSlice<T>(List<T> content, long totalElements, int totalPages, int page, int size) {
}
