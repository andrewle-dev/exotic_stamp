package metro.ExoticStamp.modules.community.application.service;

import metro.ExoticStamp.modules.community.application.command.ApplyReferralCommand;
import metro.ExoticStamp.modules.community.application.mapper.CommunityAppMapper;
import metro.ExoticStamp.modules.community.application.port.UserVerificationPort;
import metro.ExoticStamp.modules.community.application.view.ReferralView;
import metro.ExoticStamp.modules.community.config.CommunityProperties;
import metro.ExoticStamp.modules.community.domain.exception.ReferralAlreadyAppliedException;
import metro.ExoticStamp.modules.community.domain.exception.ReferralCodeInactiveException;
import metro.ExoticStamp.modules.community.domain.exception.ReferralCodeNotFoundException;
import metro.ExoticStamp.modules.community.domain.exception.SelfReferralNotAllowedException;
import metro.ExoticStamp.modules.community.domain.model.Referral;
import metro.ExoticStamp.modules.community.domain.model.ReferralCode;
import metro.ExoticStamp.modules.community.domain.model.ReferralCodeStatus;
import metro.ExoticStamp.modules.community.domain.model.ReferralStatus;
import metro.ExoticStamp.modules.community.domain.repository.ReferralCodeRepository;
import metro.ExoticStamp.modules.community.domain.repository.ReferralRepository;
import metro.ExoticStamp.modules.community.domain.service.ReferralCodeGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReferralCommandServiceTest {

    @Mock private ReferralCodeRepository referralCodeRepository;
    @Mock private ReferralRepository referralRepository;
    @Mock private UserVerificationPort userVerificationPort;
    @Mock private ApplicationEventPublisher eventPublisher;

    private ReferralCommandService service;

    @BeforeEach
    void setUp() {
        service = new ReferralCommandService(
                referralCodeRepository,
                referralRepository,
                new ReferralCodeGenerator(),
                userVerificationPort,
                new CommunityAppMapper(),
                eventPublisher
        );
    }

    @Test
    void applyValidCode_successCompletedWhenVerified() {
        UUID referrerId = UUID.randomUUID();
        UUID referredId = UUID.randomUUID();
        UUID codeId = UUID.randomUUID();
        ReferralCode code = ReferralCode.builder()
                .id(codeId).userId(referrerId).code("ABC12345").status(ReferralCodeStatus.ACTIVE).build();

        when(referralRepository.findByReferredUserId(referredId)).thenReturn(Optional.empty());
        when(referralCodeRepository.findByCode("ABC12345")).thenReturn(Optional.of(code));
        when(userVerificationPort.isEmailVerified(referredId)).thenReturn(true);
        when(referralRepository.save(any(Referral.class))).thenAnswer(inv -> {
            Referral r = inv.getArgument(0);
            r.setId(UUID.randomUUID());
            return r;
        });

        ReferralView view = service.applyReferral(referredId, new ApplyReferralCommand("abc12345"));
        assertEquals("COMPLETED", view.status());
        verify(referralCodeRepository).save(code);
    }

    @Test
    void applyUnknownCode_fails() {
        UUID referredId = UUID.randomUUID();
        when(referralRepository.findByReferredUserId(referredId)).thenReturn(Optional.empty());
        when(referralCodeRepository.findByCode("MISSING")).thenReturn(Optional.empty());
        assertThrows(ReferralCodeNotFoundException.class,
                () -> service.applyReferral(referredId, new ApplyReferralCommand("missing")));
    }

    @Test
    void applyInactiveCode_fails() {
        UUID referredId = UUID.randomUUID();
        ReferralCode code = ReferralCode.builder()
                .id(UUID.randomUUID()).userId(UUID.randomUUID()).code("OLD1").status(ReferralCodeStatus.INACTIVE).build();
        when(referralRepository.findByReferredUserId(referredId)).thenReturn(Optional.empty());
        when(referralCodeRepository.findByCode("OLD1")).thenReturn(Optional.of(code));
        assertThrows(ReferralCodeInactiveException.class,
                () -> service.applyReferral(referredId, new ApplyReferralCommand("old1")));
    }

    @Test
    void selfReferral_blocked() {
        UUID userId = UUID.randomUUID();
        ReferralCode code = ReferralCode.builder()
                .id(UUID.randomUUID()).userId(userId).code("SELF1").status(ReferralCodeStatus.ACTIVE).build();
        when(referralRepository.findByReferredUserId(userId)).thenReturn(Optional.empty());
        when(referralCodeRepository.findByCode("SELF1")).thenReturn(Optional.of(code));
        assertThrows(SelfReferralNotAllowedException.class,
                () -> service.applyReferral(userId, new ApplyReferralCommand("self1")));
    }

    @Test
    void alreadyAppliedDifferentCode_fails() {
        UUID referredId = UUID.randomUUID();
        UUID existingCodeId = UUID.randomUUID();
        Referral existing = Referral.builder()
                .id(UUID.randomUUID()).referredUserId(referredId).referralCodeId(existingCodeId).build();
        ReferralCode existingCode = ReferralCode.builder().id(existingCodeId).code("FIRST1").build();

        when(referralRepository.findByReferredUserId(referredId)).thenReturn(Optional.of(existing));
        when(referralCodeRepository.findById(existingCodeId)).thenReturn(Optional.of(existingCode));

        assertThrows(ReferralAlreadyAppliedException.class,
                () -> service.applyReferral(referredId, new ApplyReferralCommand("other1")));
        verify(referralCodeRepository, never()).findByCode("OTHER1");
    }

    @Test
    void duplicateApplySameCode_idempotent() {
        UUID referredId = UUID.randomUUID();
        UUID codeId = UUID.randomUUID();
        Referral existing = Referral.builder()
                .id(UUID.randomUUID()).referredUserId(referredId).referralCodeId(codeId)
                .status(ReferralStatus.PENDING).build();
        ReferralCode code = ReferralCode.builder().id(codeId).code("SAME1").build();

        when(referralRepository.findByReferredUserId(referredId)).thenReturn(Optional.of(existing));
        when(referralCodeRepository.findById(codeId)).thenReturn(Optional.of(code));

        ReferralView view = service.applyReferral(referredId, new ApplyReferralCommand("same1"));
        assertEquals(existing.getId(), view.id());
        verify(referralRepository, never()).save(any());
    }

    @Test
    void concurrentDuplicate_returnsExisting() {
        UUID referrerId = UUID.randomUUID();
        UUID referredId = UUID.randomUUID();
        ReferralCode code = ReferralCode.builder()
                .id(UUID.randomUUID()).userId(referrerId).code("RACE01").status(ReferralCodeStatus.ACTIVE).build();
        Referral existing = Referral.builder().id(UUID.randomUUID()).referredUserId(referredId).build();

        when(referralRepository.findByReferredUserId(referredId))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));
        when(referralCodeRepository.findByCode("RACE01")).thenReturn(Optional.of(code));
        when(userVerificationPort.isEmailVerified(referredId)).thenReturn(false);
        when(referralRepository.save(any())).thenThrow(new DataIntegrityViolationException("uq_referrals_referred_user"));

        ReferralView view = service.applyReferral(referredId, new ApplyReferralCommand("race01"));
        assertEquals(existing.getId(), view.id());
    }
}
