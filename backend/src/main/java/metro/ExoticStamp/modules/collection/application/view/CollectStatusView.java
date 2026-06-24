package metro.ExoticStamp.modules.collection.application.view;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CollectStatusView(
        CollectOutcomeStatus status,
        CollectStampResultView.StampInfo stamp,
        CollectStampResultView.ScanInfo scan,
        ProgressView progress,
        LocalDateTime createdAt,
        LocalDateTime resolvedAt,
        String errorCode
) {}
