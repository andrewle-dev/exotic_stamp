package metro.ExoticStamp.modules.community.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.community.application.command.RecordShareEventCommand;
import metro.ExoticStamp.modules.community.application.mapper.CommunityAppMapper;
import metro.ExoticStamp.modules.community.application.support.CommunityEnumParser;
import metro.ExoticStamp.modules.community.application.support.MetadataSanitizer;
import metro.ExoticStamp.modules.community.application.view.ShareEventView;
import metro.ExoticStamp.modules.community.domain.event.ShareEventRecordedEvent;
import metro.ExoticStamp.modules.community.domain.model.ShareEvent;
import metro.ExoticStamp.modules.community.domain.repository.ShareEventRepository;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShareEventCommandService {

    private final ShareEventRepository shareEventRepository;
    private final MetadataSanitizer metadataSanitizer;
    private final CommunityAppMapper communityAppMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ShareEventView recordShare(UUID userId, RecordShareEventCommand command) {
        ShareEvent event = ShareEvent.builder()
                .userId(userId)
                .platform(CommunityEnumParser.parseSharePlatform(command.platform()))
                .shareType(CommunityEnumParser.parseShareType(command.shareType()))
                .targetId(command.targetId())
                .metadata(metadataSanitizer.sanitize(command.metadata()))
                .build();

        ShareEvent saved = shareEventRepository.save(event);

        RbacTransactionCallbacks.afterCommit(() -> {
            try {
                eventPublisher.publishEvent(new ShareEventRecordedEvent(saved.getId(), userId));
            } catch (Exception e) {
                log.error("[Community] ShareEventRecordedEvent publish failed shareEventId={}: {}",
                        saved.getId(), e.getMessage(), e);
            }
        });

        return communityAppMapper.toShareEventView(saved);
    }
}
