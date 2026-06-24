package metro.ExoticStamp.common.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Stable pagination envelope for API responses (content + page metadata).
 */
@Schema(description = "Paginated list")
public record PageResponse<T>(
        @Schema(description = "Items in this page")
        List<T> content,
        @Schema(description = "Total elements across all pages")
        long totalElements,
        @Schema(description = "Total pages")
        int totalPages,
        @Schema(description = "Current page index (0-based)")
        int page,
        @Schema(description = "Page size")
        int size
) {
    public static <T> PageResponse<T> of(List<T> content, long totalElements, int totalPages, int page, int size) {
        return new PageResponse<>(content, totalElements, totalPages, page, size);
    }
}
