package metro.ExoticStamp.modules.collection.domain.model;

import java.util.List;

/**
 * Paginated slice of user stamps (domain-level, no Spring Data types).
 */
public record UserStampSlice(List<UserStamp> content, long totalElements, int totalPages, int page, int size) {}
