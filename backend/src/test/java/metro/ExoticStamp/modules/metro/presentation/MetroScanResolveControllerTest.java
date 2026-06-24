package metro.ExoticStamp.modules.metro.presentation;

import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.metro.MetroWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.metro.application.MetroScanResolveService;
import metro.ExoticStamp.modules.metro.application.view.ScanResolveStationView;
import metro.ExoticStamp.modules.metro.application.view.ScanResolveView;
import metro.ExoticStamp.modules.metro.presentation.mapper.MetroPresentationMapper;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MetroScanResolveController.class)
@Import({MetroWebMvcTestSecurityConfig.class, MetroPresentationMapper.class})
class MetroScanResolveControllerTest {

    private static final UUID STATION_ID = UUID.fromString("00000000-0000-0000-0000-000000000501");

    @Autowired private MockMvc mockMvc;
    @MockBean private MetroScanResolveService scanResolveService;
    @MockBean private JwtProvider jwtProvider;
    @MockBean private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private RoleQueryService roleQueryService;
    @MockBean private CustomAuthEntryPoint authEntryPoint;
    @MockBean private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    void resolve_public_noSecretsInResponse() throws Exception {
        when(scanResolveService.resolve(any())).thenReturn(ScanResolveView.builder()
                .resolved(true)
                .scanType("NFC")
                .station(ScanResolveStationView.builder()
                        .id(STATION_ID).code("S1").name("Station").zoneRadiusMeters(150).build())
                .build());

        mockMvc.perform(post("/api/v1/metro/scan/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"scanType\":\"NFC\",\"payload\":\"NFC1\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.station.code").value("S1"))
                .andExpect(jsonPath("$.data.scan.resolved").value(true))
                .andExpect(jsonPath("$.data.station.nfcTagId").doesNotExist())
                .andExpect(jsonPath("$.data.station.qrCodeValue").doesNotExist());
    }
}
