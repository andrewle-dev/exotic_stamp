package metro.ExoticStamp.modules.metro.presentation;

import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.metro.MetroWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.metro.application.LineQueryService;
import metro.ExoticStamp.modules.metro.application.view.LineView;
import metro.ExoticStamp.modules.metro.presentation.mapper.MetroPresentationMapper;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
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

@WebMvcTest(PublicMetroLineController.class)
@Import({MetroWebMvcTestSecurityConfig.class, MetroPresentationMapper.class})
class PublicMetroLineControllerTest {

    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");

    @Autowired private MockMvc mockMvc;
    @MockBean private LineQueryService lineQueryService;
    @MockBean private JwtProvider jwtProvider;
    @MockBean private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private RoleQueryService roleQueryService;
    @MockBean private CustomAuthEntryPoint authEntryPoint;
    @MockBean private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    void list_activeLinesPublic() throws Exception {
        when(lineQueryService.getPublicLines()).thenReturn(List.of(
                LineView.builder().id(LINE_ID).code("L1").name("Line 1").status("ACTIVE").build()));

        mockMvc.perform(get("/api/v1/metro/lines"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("L1"))
                .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
    }
}
