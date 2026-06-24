package metro.ExoticStamp.modules.collection.presentation;

import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.collection.CollectionWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.collection.application.service.CampaignCommandService;
import metro.ExoticStamp.modules.collection.application.service.CampaignQueryService;
import metro.ExoticStamp.modules.collection.application.service.CampaignStationCommandService;
import metro.ExoticStamp.modules.collection.application.service.CampaignStationQueryService;
import metro.ExoticStamp.modules.collection.application.view.CampaignView;
import metro.ExoticStamp.modules.collection.presentation.mapper.CampaignPresentationMapper;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CampaignAdminController.class)
@Import({CollectionWebMvcTestSecurityConfig.class, CampaignPresentationMapper.class})
class CampaignAdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private CampaignCommandService campaignCommandService;
    @MockBean private CampaignQueryService campaignQueryService;
    @MockBean private CampaignStationCommandService campaignStationCommandService;
    @MockBean private CampaignStationQueryService campaignStationQueryService;
    @MockBean private JwtProvider jwtProvider;
    @MockBean private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private RoleQueryService roleQueryService;
    @MockBean private CustomAuthEntryPoint authEntryPoint;
    @MockBean private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "CAMPAIGN_MANAGE"})
    void createCampaign_adminAllowed() throws Exception {
        when(campaignCommandService.create(any())).thenReturn(CampaignView.builder()
                .id(UUID.randomUUID()).code("C1").name("C").displayName("C")
                .campaignType("STANDARD").status("DRAFT")
                .startAt(LocalDateTime.now()).endAt(LocalDateTime.now().plusDays(1))
                .priority(0).build());

        mockMvc.perform(post("/api/v1/admin/campaigns").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"C1","name":"C","startAt":"2026-06-01T00:00:00","endAt":"2026-12-31T23:59:59"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createCampaign_userDenied() throws Exception {
        mockMvc.perform(post("/api/v1/admin/campaigns").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"C1","name":"C","startAt":"2026-06-01T00:00:00","endAt":"2026-12-31T23:59:59"}
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void createCampaign_unauthenticatedDenied() throws Exception {
        mockMvc.perform(post("/api/v1/admin/campaigns").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"C1","name":"C","startAt":"2026-06-01T00:00:00","endAt":"2026-12-31T23:59:59"}
                                """))
                .andExpect(status().isUnauthorized());
    }
}
