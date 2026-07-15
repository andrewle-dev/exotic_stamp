package metro.ExoticStamp.modules.reward.application.service;

import metro.ExoticStamp.modules.reward.application.mapper.RewardAppMapper;
import metro.ExoticStamp.modules.reward.application.view.PromotionalPartnerBannerView;
import metro.ExoticStamp.modules.reward.domain.model.Partner;
import metro.ExoticStamp.modules.reward.domain.repository.PartnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PartnerPromotionalQueryServiceTest {

    @Mock
    private PartnerRepository partnerRepository;

    private PartnerPromotionalQueryService service;

    @BeforeEach
    void setUp() {
        service = new PartnerPromotionalQueryService(partnerRepository, new RewardAppMapper());
    }

    @Test
    void listPromotionalBanners_filtersInactiveBlankBannerAndInvalidContract() {
        LocalDate today = LocalDate.now();
        Partner eligible = partner("Eligible", true, "https://cdn.example/a.png",
                today.minusDays(1), today.plusDays(10));
        Partner inactive = partner("Inactive", false, "https://cdn.example/b.png", null, null);
        Partner blankBanner = partner("Blank", true, "   ", null, null);
        blankBanner.setBannerImageUrl("   ");
        Partner expired = partner("Expired", true, "https://cdn.example/c.png",
                today.minusDays(30), today.minusDays(1));
        Partner noBanner = Partner.builder()
                .id(UUID.randomUUID())
                .name("NoBanner")
                .active(true)
                .logoUrl("https://cdn.example/logo.png")
                .build();

        when(partnerRepository.findActiveWithBannerOrderedByUpdatedAtDesc())
                .thenReturn(List.of(eligible, inactive, blankBanner, expired, noBanner));

        List<PromotionalPartnerBannerView> result = service.listPromotionalBanners();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).partnerName()).isEqualTo("Eligible");
        assertThat(result.get(0).bannerImageUrl()).isEqualTo("https://cdn.example/a.png");
    }

    private static Partner partner(
            String name,
            boolean active,
            String banner,
            LocalDate start,
            LocalDate end) {
        return Partner.builder()
                .id(UUID.randomUUID())
                .name(name)
                .active(active)
                .bannerImageUrl(banner)
                .contractStartDate(start)
                .contractEndDate(end)
                .build();
    }
}
