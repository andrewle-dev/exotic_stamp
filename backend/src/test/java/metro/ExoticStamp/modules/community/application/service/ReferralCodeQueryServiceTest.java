package metro.ExoticStamp.modules.community.application.service;

import metro.ExoticStamp.modules.community.application.mapper.CommunityAppMapper;
import metro.ExoticStamp.modules.community.application.view.ReferralCodeView;
import metro.ExoticStamp.modules.community.config.CommunityProperties;
import metro.ExoticStamp.modules.community.domain.model.ReferralCode;
import metro.ExoticStamp.modules.community.domain.model.ReferralCodeStatus;
import metro.ExoticStamp.modules.community.domain.repository.ReferralCodeRepository;
import metro.ExoticStamp.modules.community.domain.service.ReferralCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferralCodeQueryServiceTest {

    @Mock private ReferralCodeRepository referralCodeRepository;

    private ReferralCodeQueryService service;

    @BeforeEach
    void setUp() {
        CommunityProperties props = new CommunityProperties();
        props.setReferralCodeMaxAttempts(3);
        service = new ReferralCodeQueryService(
                referralCodeRepository,
                new ReferralCodeGenerator(),
                new CommunityAppMapper(),
                props
        );
    }

    @Test
    void getOrCreate_returnsExisting() {
        UUID userId = UUID.randomUUID();
        ReferralCode existing = ReferralCode.builder()
                .id(UUID.randomUUID()).userId(userId).code("EXIST01").status(ReferralCodeStatus.ACTIVE).build();
        when(referralCodeRepository.findByUserId(userId)).thenReturn(Optional.of(existing));

        ReferralCodeView view = service.getOrCreateMyReferralCode(userId);
        assertEquals("EXIST01", view.code());
    }

    @Test
    void getOrCreate_createsWhenMissing() {
        UUID userId = UUID.randomUUID();
        when(referralCodeRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(referralCodeRepository.save(any(ReferralCode.class))).thenAnswer(inv -> {
            ReferralCode c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        ReferralCodeView view = service.getOrCreateMyReferralCode(userId);
        assertNotNull(view.code());
        assertEquals(8, view.code().length());
    }

    @Test
    void getOrCreate_returnsExistingOnUserRace() {
        UUID userId = UUID.randomUUID();
        ReferralCode existing = ReferralCode.builder()
                .id(UUID.randomUUID()).userId(userId).code("RACE02").status(ReferralCodeStatus.ACTIVE).build();
        when(referralCodeRepository.findByUserId(userId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));
        when(referralCodeRepository.save(any())).thenThrow(
                new DataIntegrityViolationException("uq_referral_codes_user_id"));

        ReferralCodeView view = service.getOrCreateMyReferralCode(userId);
        assertEquals("RACE02", view.code());
    }
}
