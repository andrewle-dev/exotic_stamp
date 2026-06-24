package metro.ExoticStamp.common.model;

import java.util.List;

public record PageResult<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int currentPage
) {
    public static <T> PageResult<T> of(List<T> content, long total, int totalPages, int page) {
        return new PageResult<>(content, total, totalPages, page);
    }
}
