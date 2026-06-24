package metro.ExoticStamp.modules.community.presentation.controller;

import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.community.CommunityWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.community.application.service.ReferralCodeQueryService;
import metro.ExoticStamp.modules.community.application.service.ReferralCommandService;
import metro.ExoticStamp.modules.community.application.service.ReferralQueryService;
import metro.ExoticStamp.modules.community.application.view.MyReferralsView;
import metro.ExoticStamp.modules.community.application.view.ReferralCodeView;
import metro.ExoticStamp.modules.community.domain.model.ReferralCodeStatus;
import metro.ExoticStamp.modules.community.presentation.mapper.CommunityPresentationMapper;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CommunityReferralController.class)
@Import({CommunityWebMvcTestSecurityConfig.class, CommunityPresentationMapper.class})
class CommunityReferralControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private ReferralCodeQueryService referralCodeQueryService;
    @MockBean private ReferralCommandService referralCommandService;
    @MockBean private ReferralQueryService referralQueryService;
    @MockBean private JwtProvider jwtProvider;
    @MockBean private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private RoleQueryService roleQueryService;
    @MockBean private CustomAuthEntryPoint authEntryPoint;
    @MockBean private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    void unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/community/referral-code"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001")
    void getReferralCode_success() throws Exception {
        when(referralCodeQueryService.getOrCreateMyReferralCode(any())).thenReturn(ReferralCodeView.builder()
                .id(UUID.randomUUID())
                .code("ABCD1234")
                .status("ACTIVE")
                .totalReferrals(0)
                .createdAt(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/v1/community/referral-code"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").value("ABCD1234"));
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001")
    void getMyReferrals_success() throws Exception {
        when(referralQueryService.getMyReferrals(any())).thenReturn(MyReferralsView.builder()
                .referredBy(null)
                .referredUsers(List.of())
                .pendingCount(0)
                .completedCount(0)
                .rewardedCount(0)
                .build());

        mockMvc.perform(get("/api/v1/community/referrals/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pendingCount").value(0));
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001")
    void applyReferral_success() throws Exception {
        when(referralCommandService.applyReferral(any(), any())).thenReturn(
                metro.ExoticStamp.modules.community.application.view.ReferralView.builder()
                        .id(UUID.randomUUID())
                        .referrerUserId(UUID.randomUUID())
                        .referredUserId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                        .referralCodeId(UUID.randomUUID())
                        .status("PENDING")
                        .appliedAt(LocalDateTime.now())
                        .build());

        mockMvc.perform(post("/api/v1/community/referrals/apply").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"FRIEND1\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }
}
