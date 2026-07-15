package metro.ExoticStamp.modules.reward.presentation.controller;

import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.modules.reward.RewardWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.reward.application.service.PartnerPromotionalQueryService;
import metro.ExoticStamp.modules.reward.application.view.PromotionalPartnerBannerView;
import metro.ExoticStamp.modules.reward.presentation.mapper.RewardPresentationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PartnerPublicController.class)
@Import({RewardWebMvcTestSecurityConfig.class, RewardPresentationMapper.class})
class PartnerPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PartnerPromotionalQueryService partnerPromotionalQueryService;
    @MockBean
    private JwtProvider jwtProvider;
    @MockBean
    private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean
    private UserDetailsServiceImpl userDetailsService;
    @MockBean
    private RoleQueryService roleQueryService;
    @MockBean
    private CustomAuthEntryPoint authEntryPoint;
    @MockBean
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    void listPromotionalBanners_publicAccess() throws Exception {
        UUID partnerId = UUID.randomUUID();
        when(partnerPromotionalQueryService.listPromotionalBanners()).thenReturn(List.of(
                PromotionalPartnerBannerView.builder()
                        .partnerId(partnerId)
                        .partnerName("Highland Coffee")
                        .logoUrl("https://cdn.example/logo.png")
                        .bannerImageUrl("https://cdn.example/banner.png")
                        .build()));

        mockMvc.perform(get("/api/v1/partners/promotional-banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].partnerName").value("Highland Coffee"))
                .andExpect(jsonPath("$.data[0].bannerImageUrl").value("https://cdn.example/banner.png"));
    }
}
