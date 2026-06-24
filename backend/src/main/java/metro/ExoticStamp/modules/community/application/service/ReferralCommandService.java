package metro.ExoticStamp.modules.community.application.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import metro.ExoticStamp.modules.community.application.command.ApplyReferralCommand;
import metro.ExoticStamp.modules.community.application.mapper.CommunityAppMapper;
import metro.ExoticStamp.modules.community.application.port.UserVerificationPort;
import metro.ExoticStamp.modules.community.application.view.ReferralView;
import metro.ExoticStamp.modules.community.domain.event.ReferralAppliedEvent;
import metro.ExoticStamp.modules.community.domain.event.ReferralCompletedEvent;
import metro.ExoticStamp.modules.community.domain.exception.ReferralAlreadyAppliedException;
import metro.ExoticStamp.modules.community.domain.exception.ReferralCodeInactiveException;
import metro.ExoticStamp.modules.community.domain.exception.ReferralCodeNotFoundException;
import metro.ExoticStamp.modules.community.domain.exception.ReferralConflictException;
import metro.ExoticStamp.modules.community.domain.exception.SelfReferralNotAllowedException;
import metro.ExoticStamp.modules.community.domain.model.Referral;
import metro.ExoticStamp.modules.community.domain.model.ReferralCode;
import metro.ExoticStamp.modules.community.domain.model.ReferralStatus;
import metro.ExoticStamp.modules.community.domain.repository.ReferralCodeRepository;
import metro.ExoticStamp.modules.community.domain.repository.ReferralRepository;
import metro.ExoticStamp.modules.community.domain.service.ReferralCodeGenerator;
import metro.ExoticStamp.modules.rbac.application.support.RbacTransactionCallbacks;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReferralCommandService {

    private final ReferralCodeRepository referralCodeRepository;
    private final ReferralRepository referralRepository;
    private final ReferralCodeGenerator referralCodeGenerator;
    private final UserVerificationPort userVerificationPort;
    private final CommunityAppMapper communityAppMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ReferralView applyReferral(UUID referredUserId, ApplyReferralCommand command) {
        String normalized = referralCodeGenerator.normalize(command.code());
        if (normalized.isBlank()) {
            throw new ReferralCodeNotFoundException("Invalid referral code");
        }

        Optional<Referral> existing = referralRepository.findByReferredUserId(referredUserId);
        if (existing.isPresent()) {
            Referral current = existing.get();
            ReferralCode currentCode = referralCodeRepository.findById(current.getReferralCodeId()).orElse(null);
            if (currentCode != null && normalized.equals(currentCode.getCode())) {
                return communityAppMapper.toReferralView(current);
            }
            throw new ReferralAlreadyAppliedException("Referral already applied");
        }

        ReferralCode referralCode = referralCodeRepository.findByCode(normalized)
                .orElseThrow(() -> new ReferralCodeNotFoundException("Referral code not found"));

        if (!referralCode.isActive()) {
            throw new ReferralCodeInactiveException("Referral code is inactive");
        }
        if (referralCode.getUserId().equals(referredUserId)) {
            throw new SelfReferralNotAllowedException("Self referral is not allowed");
        }

        ReferralStatus initialStatus = userVerificationPort.isEmailVerified(referredUserId)
                ? ReferralStatus.COMPLETED
                : ReferralStatus.PENDING;
        LocalDateTime now = LocalDateTime.now();

        Referral referral = Referral.builder()
                .referrerUserId(referralCode.getUserId())
                .referredUserId(referredUserId)
                .referralCodeId(referralCode.getId())
                .status(initialStatus)
                .referredAt(now)
                .completedAt(initialStatus == ReferralStatus.COMPLETED ? now : null)
                .build();

        Referral saved;
        try {
            saved = referralRepository.save(referral);
        } catch (DataIntegrityViolationException ex) {
            if (isReferredUserUniqueViolation(ex)) {
                return referralRepository.findByReferredUserId(referredUserId)
                        .map(communityAppMapper::toReferralView)
                        .orElseThrow(() -> new ReferralConflictException("Referral conflict"));
            }
            throw ex;
        }

        referralCode.setTotalReferrals(referralCode.getTotalReferrals() + 1);
        referralCodeRepository.save(referralCode);

        publishEvents(saved, initialStatus);
        return communityAppMapper.toReferralView(saved);
    }

    @Transactional
    public void completePendingReferral(UUID referredUserId) {
        referralRepository.findByReferredUserId(referredUserId).ifPresent(referral -> {
            if (referral.getStatus() != ReferralStatus.PENDING) {
                return;
            }
            LocalDateTime now = LocalDateTime.now();
            referral.setStatus(ReferralStatus.COMPLETED);
            referral.setCompletedAt(now);
            Referral saved = referralRepository.save(referral);
            publishCompletedEvent(saved);
        });
    }

    private void publishEvents(Referral saved, ReferralStatus initialStatus) {
        RbacTransactionCallbacks.afterCommit(() -> {
            try {
                eventPublisher.publishEvent(new ReferralAppliedEvent(
                        saved.getId(),
                        saved.getReferrerUserId(),
                        saved.getReferredUserId(),
                        saved.getReferralCodeId()));
                if (initialStatus == ReferralStatus.COMPLETED) {
                    eventPublisher.publishEvent(new ReferralCompletedEvent(
                            saved.getId(),
                            saved.getReferrerUserId(),
                            saved.getReferredUserId()));
                }
            } catch (Exception e) {
                log.error("[Community] referral event publish failed referralId={}: {}",
                        saved.getId(), e.getMessage(), e);
            }
        });
    }

    private void publishCompletedEvent(Referral saved) {
        RbacTransactionCallbacks.afterCommit(() -> {
            try {
                eventPublisher.publishEvent(new ReferralCompletedEvent(
                        saved.getId(),
                        saved.getReferrerUserId(),
                        saved.getReferredUserId()));
            } catch (Exception e) {
                log.error("[Community] ReferralCompletedEvent publish failed referralId={}: {}",
                        saved.getId(), e.getMessage(), e);
            }
        });
    }

    private static boolean isReferredUserUniqueViolation(DataIntegrityViolationException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        return msg != null && msg.contains("uq_referrals_referred_user");
    }
}
