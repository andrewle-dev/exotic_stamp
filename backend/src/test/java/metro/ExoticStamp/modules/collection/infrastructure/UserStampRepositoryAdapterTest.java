package metro.ExoticStamp.modules.collection.infrastructure;

import metro.ExoticStamp.modules.collection.domain.model.UserStamp;
import metro.ExoticStamp.modules.collection.domain.model.UserStampSlice;
import metro.ExoticStamp.modules.collection.infrastructure.repository.JpaUserStampRepository;
import metro.ExoticStamp.modules.collection.infrastructure.repository.UserStampRepositoryAdapter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserStampRepositoryAdapterTest {

    @Mock
    private JpaUserStampRepository jpa;

    @InjectMocks
    private UserStampRepositoryAdapter adapter;

    @Test
    void findByUserIdAndCampaignIdPaged_delegates() {
        UUID userId = UUID.randomUUID();
        UUID campaignId = UUID.randomUUID();
        Page<UserStamp> page = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);
        when(jpa.findByUserIdAndCampaignIdOrderByCollectedAtDesc(eq(userId), eq(campaignId), eq(PageRequest.of(0, 10))))
                .thenReturn(page);

        UserStampSlice slice = adapter.findByUserIdAndCampaignIdPaged(userId, campaignId, 0, 10);
        assertEquals(0, slice.content().size());
        verify(jpa).findByUserIdAndCampaignIdOrderByCollectedAtDesc(eq(userId), eq(campaignId), eq(PageRequest.of(0, 10)));
    }
}
