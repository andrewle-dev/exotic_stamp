package metro.ExoticStamp.modules.reward.domain.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PartnerEligibilityTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 7, 15);

    @Test
    void withinContract_whenDatesNull_isOpenEnded() {
        Partner p = Partner.builder().name("A").active(true).build();
        assertThat(p.isWithinContractWindow(TODAY)).isTrue();
    }

    @Test
    void withinContract_rejectsBeforeStart() {
        Partner p = Partner.builder()
                .name("A")
                .active(true)
                .contractStartDate(TODAY.plusDays(1))
                .build();
        assertThat(p.isWithinContractWindow(TODAY)).isFalse();
    }

    @Test
    void withinContract_rejectsAfterEnd() {
        Partner p = Partner.builder()
                .name("A")
                .active(true)
                .contractEndDate(TODAY.minusDays(1))
                .build();
        assertThat(p.isWithinContractWindow(TODAY)).isFalse();
    }

    @Test
    void withinContract_inclusiveBounds() {
        Partner p = Partner.builder()
                .name("A")
                .active(true)
                .contractStartDate(TODAY)
                .contractEndDate(TODAY)
                .build();
        assertThat(p.isWithinContractWindow(TODAY)).isTrue();
    }

    @Test
    void eligible_requiresActiveBannerAndContract() {
        Partner eligible = Partner.builder()
                .name("Highland")
                .active(true)
                .bannerImageUrl("https://cdn.example/banner.png")
                .contractStartDate(TODAY.minusDays(1))
                .contractEndDate(TODAY.plusDays(30))
                .build();
        assertThat(eligible.isEligibleForPromotion(TODAY)).isTrue();

        Partner inactive = Partner.builder()
                .name("X")
                .active(false)
                .bannerImageUrl("https://cdn.example/banner.png")
                .build();
        assertThat(inactive.isEligibleForPromotion(TODAY)).isFalse();

        Partner noBanner = Partner.builder()
                .name("Y")
                .active(true)
                .logoUrl("https://cdn.example/logo.png")
                .build();
        assertThat(noBanner.isEligibleForPromotion(TODAY)).isFalse();

        Partner expired = Partner.builder()
                .name("Z")
                .active(true)
                .bannerImageUrl("https://cdn.example/banner.png")
                .contractEndDate(TODAY.minusDays(1))
                .build();
        assertThat(expired.isEligibleForPromotion(TODAY)).isFalse();
    }

    @Test
    void eligible_withoutBanner_partnerStillValidEntity() {
        Partner logoOnly = Partner.builder()
                .name("Logo Only")
                .active(true)
                .logoUrl("https://cdn.example/logo.png")
                .build();
        assertThat(logoOnly.getLogoUrl()).isEqualTo("https://cdn.example/logo.png");
        assertThat(logoOnly.isEligibleForPromotion(TODAY)).isFalse();
    }
}
