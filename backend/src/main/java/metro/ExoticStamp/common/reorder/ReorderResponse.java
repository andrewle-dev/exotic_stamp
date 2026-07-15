package metro.ExoticStamp.common.reorder;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReorderResponse {
    private UUID scopeId;
    private int updatedCount;
    private List<ReorderItemResponse> items;

    public static ReorderResponse from(ReorderResultView view) {
        return ReorderResponse.builder()
                .scopeId(view.scopeId())
                .updatedCount(view.updatedCount())
                .items(view.items().stream()
                        .map(item -> ReorderItemResponse.builder()
                                .id(item.id())
                                .sortOrder(item.sortOrder())
                                .build())
                        .toList())
                .build();
    }
}
