package metro.ExoticStamp.modules.reward.presentation.controller;

import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.modules.reward.RewardWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.reward.application.service.AdminRewardCommandService;
import metro.ExoticStamp.modules.reward.application.service.AdminRewardQueryService;
import metro.ExoticStamp.modules.reward.application.view.PartnerView;
import metro.ExoticStamp.modules.reward.presentation.mapper.RewardPresentationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminPartnerController.class)
@Import({RewardWebMvcTestSecurityConfig.class, RewardPresentationMapper.class})
class AdminPartnerControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AdminRewardQueryService adminRewardQueryService;
    @MockBean private AdminRewardCommandService adminRewardCommandService;
    @MockBean private JwtProvider jwtProvider;
    @MockBean private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private RoleQueryService roleQueryService;
    @MockBean private CustomAuthEntryPoint authEntryPoint;
    @MockBean private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPartner_adminAllowed() throws Exception {
        UUID id = UUID.randomUUID();
        when(adminRewardCommandService.createPartner(any())).thenReturn(PartnerView.builder()
                .id(id)
                .name("Partner A")
                .contactEmail("a@partner.test")
                .active(true)
                .build());

        mockMvc.perform(post("/api/v1/admin/partners").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Partner A","contactEmail":"a@partner.test"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Partner A"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createPartner_persistsBannerImageUrl() throws Exception {
        UUID id = UUID.randomUUID();
        when(adminRewardCommandService.createPartner(any())).thenReturn(PartnerView.builder()
                .id(id)
                .name("Highland")
                .logoUrl("https://cdn.example/logo.png")
                .bannerImageUrl("https://cdn.example/banner.png")
                .contactEmail("partner@highland.com")
                .active(true)
                .build());

        mockMvc.perform(post("/api/v1/admin/partners").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name":"Highland",
                                  "logoUrl":"https://cdn.example/logo.png",
                                  "bannerImageUrl":"https://cdn.example/banner.png",
                                  "contactEmail":"partner@highland.com"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.bannerImageUrl").value("https://cdn.example/banner.png"))
                .andExpect(jsonPath("$.data.logoUrl").value("https://cdn.example/logo.png"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getPartner_returnsBannerImageUrl() throws Exception {
        UUID id = UUID.randomUUID();
        when(adminRewardQueryService.getPartner(id)).thenReturn(PartnerView.builder()
                .id(id)
                .name("Highland")
                .bannerImageUrl("https://cdn.example/banner.png")
                .active(true)
                .build());

        mockMvc.perform(get("/api/v1/admin/partners/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.bannerImageUrl").value("https://cdn.example/banner.png"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void createPartner_userDenied() throws Exception {
        mockMvc.perform(post("/api/v1/admin/partners").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Partner A","contactEmail":"a@partner.test"}
                                """))
                .andExpect(status().isForbidden());
    }
}
