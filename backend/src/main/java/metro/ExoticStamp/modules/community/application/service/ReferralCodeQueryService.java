package metro.ExoticStamp.modules.community.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.community.application.mapper.CommunityAppMapper;
import metro.ExoticStamp.modules.community.application.view.ReferralCodeView;
import metro.ExoticStamp.modules.community.config.CommunityProperties;
import metro.ExoticStamp.modules.community.domain.model.ReferralCode;
import metro.ExoticStamp.modules.community.domain.model.ReferralCodeStatus;
import metro.ExoticStamp.modules.community.domain.repository.ReferralCodeRepository;
import metro.ExoticStamp.modules.community.domain.service.ReferralCodeGenerator;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReferralCodeQueryService {

    private final ReferralCodeRepository referralCodeRepository;
    private final ReferralCodeGenerator referralCodeGenerator;
    private final CommunityAppMapper communityAppMapper;
    private final CommunityProperties communityProperties;

    @Transactional
    public ReferralCodeView getOrCreateMyReferralCode(UUID userId) {
        return referralCodeRepository.findByUserId(userId)
                .map(communityAppMapper::toReferralCodeView)
                .orElseGet(() -> communityAppMapper.toReferralCodeView(createUniqueCode(userId)));
    }

    private ReferralCode createUniqueCode(UUID userId) {
        int attempts = Math.max(1, communityProperties.getReferralCodeMaxAttempts());
        for (int i = 0; i < attempts; i++) {
            String code = referralCodeGenerator.generate();
            ReferralCode entity = ReferralCode.builder()
                    .userId(userId)
                    .code(code)
                    .status(ReferralCodeStatus.ACTIVE)
                    .build();
            try {
                return referralCodeRepository.save(entity);
            } catch (DataIntegrityViolationException ex) {
                if (isUserUniqueViolation(ex)) {
                    return referralCodeRepository.findByUserId(userId)
                            .orElseThrow(() -> ex);
                }
                log.debug("[Community] referral code collision, retrying userId={}", userId);
            }
        }
        throw new IllegalStateException("Unable to generate unique referral code");
    }

    private static boolean isUserUniqueViolation(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        return msg != null && msg.contains("uq_referral_codes_user_id");
    }
}
