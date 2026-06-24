package metro.ExoticStamp.modules.collection.presentation;

import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.collection.CollectionWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.collection.application.service.ActiveCampaignQueryService;
import metro.ExoticStamp.modules.collection.presentation.mapper.CampaignPresentationMapper;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CampaignPublicController.class)
@Import({CollectionWebMvcTestSecurityConfig.class, CampaignPresentationMapper.class})
class CampaignPublicControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ActiveCampaignQueryService activeCampaignQueryService;
    @MockBean private JwtProvider jwtProvider;
    @MockBean private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private RoleQueryService roleQueryService;
    @MockBean private CustomAuthEntryPoint authEntryPoint;
    @MockBean private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    void listActive_publicAccess() throws Exception {
        when(activeCampaignQueryService.listActive()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/campaigns/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.campaigns").isArray());
    }
}
