package metro.ExoticStamp.modules.community.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import metro.ExoticStamp.modules.community.application.command.RecordShareEventCommand;
import metro.ExoticStamp.modules.community.application.mapper.CommunityAppMapper;
import metro.ExoticStamp.modules.community.application.support.MetadataSanitizer;
import metro.ExoticStamp.modules.community.application.view.ShareEventView;
import metro.ExoticStamp.modules.community.config.CommunityProperties;
import metro.ExoticStamp.modules.community.domain.exception.SharePlatformInvalidException;
import metro.ExoticStamp.modules.community.domain.exception.ShareTypeInvalidException;
import metro.ExoticStamp.modules.community.domain.model.ShareEvent;
import metro.ExoticStamp.modules.community.domain.model.SharePlatform;
import metro.ExoticStamp.modules.community.domain.model.ShareType;
import metro.ExoticStamp.modules.community.domain.repository.ShareEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShareEventCommandServiceTest {

    @Mock private ShareEventRepository shareEventRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    private ShareEventCommandService service;

    @BeforeEach
    void setUp() {
        CommunityProperties props = new CommunityProperties();
        props.setMaxMetadataBytes(2048);
        service = new ShareEventCommandService(
                shareEventRepository,
                new MetadataSanitizer(new ObjectMapper(), props),
                new CommunityAppMapper(),
                eventPublisher
        );
    }

    @Test
    void recordFacebookShare_success() {
        UUID userId = UUID.randomUUID();
        when(shareEventRepository.save(any(ShareEvent.class))).thenAnswer(inv -> {
            ShareEvent e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        ShareEventView view = service.recordShare(userId, new RecordShareEventCommand(
                "FACEBOOK", "STAMP_BOOK", null, Map.of("screen", "book")));

        assertEquals("FACEBOOK", view.platform());
        assertEquals("STAMP_BOOK", view.shareType());
    }

    @Test
    void invalidPlatform_fails() {
        assertThrows(SharePlatformInvalidException.class, () -> service.recordShare(
                UUID.randomUUID(), new RecordShareEventCommand("TWITTER", "OTHER", null, null)));
    }

    @Test
    void invalidShareType_fails() {
        assertThrows(ShareTypeInvalidException.class, () -> service.recordShare(
                UUID.randomUUID(), new RecordShareEventCommand("ZALO", "INVALID", null, null)));
    }

    @Test
    void metadataSanitized_removesSensitiveKeys() {
        UUID userId = UUID.randomUUID();
        when(shareEventRepository.save(any(ShareEvent.class))).thenAnswer(inv -> {
            ShareEvent e = inv.getArgument(0);
            e.setId(UUID.randomUUID());
            return e;
        });

        service.recordShare(userId, new RecordShareEventCommand(
                "ZALO", "REFERRAL", null, Map.of("password", "secret", "note", "ok")));

        ArgumentCaptor<ShareEvent> cap = ArgumentCaptor.forClass(ShareEvent.class);
        verify(shareEventRepository).save(cap.capture());
        assertFalse(cap.getValue().getMetadata().containsKey("password"));
        assertEquals("ok", cap.getValue().getMetadata().get("note"));
    }
}
