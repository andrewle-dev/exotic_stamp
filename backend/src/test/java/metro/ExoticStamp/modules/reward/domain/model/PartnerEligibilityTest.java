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

    @Test
    void withinContract_nullToday_isFalse() {
        Partner p = Partner.builder().name("A").active(true).build();
        assertThat(p.isWithinContractWindow(null)).isFalse();
    }

    @Test
    void withinContract_onlyStartDateBeforeToday_isTrue() {
        Partner p = Partner.builder()
                .name("A")
                .active(true)
                .contractStartDate(TODAY.minusDays(1))
                .build();
        assertThat(p.isWithinContractWindow(TODAY)).isTrue();
    }

    @Test
    void withinContract_onlyEndDateAfterToday_isTrue() {
        Partner p = Partner.builder()
                .name("A")
                .active(true)
                .contractEndDate(TODAY.plusDays(1))
                .build();
        assertThat(p.isWithinContractWindow(TODAY)).isTrue();
    }

    @Test
    void eligible_blankBanner_isFalse() {
        Partner blankBanner = Partner.builder()
                .name("Blank")
                .active(true)
                .bannerImageUrl("   ")
                .build();
        assertThat(blankBanner.isEligibleForPromotion(TODAY)).isFalse();
    }

    @Test
    void onPrePersist_normalizesBlankBannerToNull() {
        Partner p = Partner.builder()
                .name("  Partner  ")
                .active(true)
                .bannerImageUrl("   ")
                .logoUrl(" https://cdn/logo.png ")
                .contactEmail(" partner@test.com ")
                .build();
        p.onPrePersist();
        assertThat(p.getName()).isEqualTo("Partner");
        assertThat(p.getBannerImageUrl()).isNull();
        assertThat(p.getLogoUrl()).isEqualTo("https://cdn/logo.png");
        assertThat(p.getContactEmail()).isEqualTo("partner@test.com");
    }

    @Test
    void validate_rejectsBlankName() {
        Partner p = Partner.builder().name("   ").active(true).build();
        org.assertj.core.api.Assertions.assertThatThrownBy(p::onPrePersist)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name");
    }

    @Test
    void eligible_nullBanner_isFalse() {
        Partner p = Partner.builder().name("A").active(true).build();
        assertThat(p.isEligibleForPromotion(TODAY)).isFalse();
    }

    @Test
    void validate_rejectsOverlongFields() {
        Partner longName = Partner.builder().name("x".repeat(101)).active(true).build();
        org.assertj.core.api.Assertions.assertThatThrownBy(longName::onPrePersist)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name length");

        Partner longLogo = Partner.builder()
                .name("Ok")
                .active(true)
                .logoUrl("https://cdn.example/" + "a".repeat(500))
                .build();
        org.assertj.core.api.Assertions.assertThatThrownBy(longLogo::onPrePersist)
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("logoUrl");
    }
}
