package metro.ExoticStamp.modules.metro.presentation;

import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.metro.MetroWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.metro.application.StationCommandService;
import metro.ExoticStamp.modules.metro.application.StationQueryService;
import metro.ExoticStamp.modules.metro.application.view.StationDetailView;
import metro.ExoticStamp.modules.metro.presentation.mapper.MetroPresentationMapper;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminMetroStationController.class)
@Import({MetroWebMvcTestSecurityConfig.class, MetroPresentationMapper.class})
class AdminMetroStationControllerTest {

    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");

    @Autowired private MockMvc mockMvc;
    @MockBean private StationQueryService stationQueryService;
    @MockBean private StationCommandService stationCommandService;
    @MockBean private JwtProvider jwtProvider;
    @MockBean private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private RoleQueryService roleQueryService;
    @MockBean private CustomAuthEntryPoint authEntryPoint;
    @MockBean private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "METRO_STATION_MANAGE"})
    void createStation_created() throws Exception {
        when(stationCommandService.createStation(any())).thenReturn(
                StationDetailView.builder().id(STATION_ID).lineId(LINE_ID).code("S1").name("Station").status("DRAFT").build());

        mockMvc.perform(post("/api/v1/admin/metro/stations").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lineId\":\"" + LINE_ID + "\",\"code\":\"S1\",\"name\":\"Station\",\"sortOrder\":1}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("S1"));
    }

    @Test
    void createStation_unauthenticated_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/metro/stations").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lineId\":\"" + LINE_ID + "\",\"code\":\"S1\",\"name\":\"Station\",\"sortOrder\":1}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createStation_nonAdmin_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/metro/stations").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"lineId\":\"" + LINE_ID + "\",\"code\":\"S1\",\"name\":\"Station\",\"sortOrder\":1}"))
                .andExpect(status().isForbidden());
    }
}
