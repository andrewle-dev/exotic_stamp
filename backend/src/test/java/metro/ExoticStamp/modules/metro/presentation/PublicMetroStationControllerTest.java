package metro.ExoticStamp.modules.metro.presentation;

import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.metro.MetroWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.metro.application.StationQueryService;
import metro.ExoticStamp.modules.metro.application.view.StationView;
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

@WebMvcTest(PublicMetroStationController.class)
@Import({MetroWebMvcTestSecurityConfig.class, MetroPresentationMapper.class})
class PublicMetroStationControllerTest {

    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");

    @Autowired private MockMvc mockMvc;
    @MockBean private StationQueryService stationQueryService;
    @MockBean private JwtProvider jwtProvider;
    @MockBean private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private RoleQueryService roleQueryService;
    @MockBean private CustomAuthEntryPoint authEntryPoint;
    @MockBean private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    void list_activeStationsPublic_noScanSecrets() throws Exception {
        when(stationQueryService.getPublicStations(null)).thenReturn(List.of(
                StationView.builder().id(STATION_ID).lineId(LINE_ID).code("S1").name("Station")
                        .lineCode("L1").lineName("Line 1").status("ACTIVE").build()));

        mockMvc.perform(get("/api/v1/metro/stations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("S1"))
                .andExpect(jsonPath("$.data[0].nfcTagId").doesNotExist())
                .andExpect(jsonPath("$.data[0].qrCodeValue").doesNotExist());
    }

    @Test
    void getPublicStationDetail_active_noScanSecrets() throws Exception {
        when(stationQueryService.getPublicStationDetail(STATION_ID)).thenReturn(
                metro.ExoticStamp.modules.metro.application.view.StationDetailView.builder()
                        .id(STATION_ID).lineId(LINE_ID).code("S1").name("Station").status("ACTIVE").build());

        mockMvc.perform(get("/api/v1/metro/stations/{id}", STATION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("S1"))
                .andExpect(jsonPath("$.data.nfcTagId").doesNotExist())
                .andExpect(jsonPath("$.data.qrCodeValue").doesNotExist())
                .andExpect(jsonPath("$.data.scanKeyStatus").doesNotExist());
    }

    @Test
    void listByLine_activeStationsPublic() throws Exception {
        when(stationQueryService.getPublicStationsByLine(LINE_ID)).thenReturn(List.of(
                StationView.builder().id(STATION_ID).lineId(LINE_ID).code("S1").status("ACTIVE").build()));

        mockMvc.perform(get("/api/v1/metro/lines/{lineId}/stations", LINE_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].code").value("S1"));
    }
}
