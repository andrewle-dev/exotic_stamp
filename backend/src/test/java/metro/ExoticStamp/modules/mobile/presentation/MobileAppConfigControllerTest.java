package metro.ExoticStamp.modules.mobile.presentation;

import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.mobile.MobileWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.mobile.application.service.MobileAppConfigQueryService;
import metro.ExoticStamp.modules.mobile.application.view.MaintenancePolicyView;
import metro.ExoticStamp.modules.mobile.application.view.MobileAppConfigView;
import metro.ExoticStamp.modules.mobile.application.view.PlatformVersionPolicyView;
import metro.ExoticStamp.modules.mobile.presentation.mapper.MobileAppConfigPresentationMapper;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MobileAppConfigController.class)
@Import({MobileWebMvcTestSecurityConfig.class, MobileAppConfigPresentationMapper.class})
class MobileAppConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MobileAppConfigQueryService mobileAppConfigQueryService;
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
    void getAppConfig_isPublicAndReturnsPlatformPolicies() throws Exception {
        when(mobileAppConfigQueryService.getAppConfig()).thenReturn(MobileAppConfigView.builder()
                .android(PlatformVersionPolicyView.builder()
                        .minimumSupportedVersion("0.1.0")
                        .latestVersion("0.2.0")
                        .forceUpdate(false)
                        .storeUrl("https://play.google.com/store/apps/details?id=com.example")
                        .build())
                .ios(PlatformVersionPolicyView.builder()
                        .minimumSupportedVersion("0.1.0")
                        .latestVersion("0.2.0")
                        .forceUpdate(true)
                        .storeUrl(null)
                        .build())
                .maintenance(MaintenancePolicyView.builder()
                        .enabled(false)
                        .message(null)
                        .build())
                .build());

        mockMvc.perform(get("/api/v1/mobile/app-config"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.android.minimumSupportedVersion").value("0.1.0"))
                .andExpect(jsonPath("$.data.android.latestVersion").value("0.2.0"))
                .andExpect(jsonPath("$.data.android.forceUpdate").value(false))
                .andExpect(jsonPath("$.data.android.storeUrl")
                        .value("https://play.google.com/store/apps/details?id=com.example"))
                .andExpect(jsonPath("$.data.ios.forceUpdate").value(true))
                .andExpect(jsonPath("$.data.ios.storeUrl").value(nullValue()))
                .andExpect(jsonPath("$.data.maintenance.enabled").value(false))
                .andExpect(jsonPath("$.data.maintenance.message").value(nullValue()));
    }
}
