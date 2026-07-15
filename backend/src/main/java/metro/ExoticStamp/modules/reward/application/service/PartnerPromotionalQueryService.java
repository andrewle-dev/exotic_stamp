package metro.ExoticStamp.modules.reward.application.service;

import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.application.view.PromotionalPartnerBannerView;
import metro.ExoticStamp.modules.reward.domain.repository.PartnerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mobile-facing partner promotion feed for Home carousel.
 * <p>
 * Eligibility: active + non-blank bannerImageUrl + within contract window (null dates = open).
 * Sort: updatedAt descending (repository order preserved after contract filter).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PartnerPromotionalQueryService {

    private final PartnerRepository partnerRepository;
    private final RewardAppMapper rewardAppMapper;

    public List<PromotionalPartnerBannerView> listPromotionalBanners() {
        LocalDate today = LocalDate.now();
        return partnerRepository.findActiveWithBannerOrderedByUpdatedAtDesc().stream()
                .filter(p -> p.isEligibleForPromotion(today))
                .map(rewardAppMapper::toPromotionalPartnerBannerView)
                .collect(Collectors.toList());
    }
}
