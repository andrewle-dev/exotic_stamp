package metro.ExoticStamp.modules.reward.presentation.controller;

import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.modules.reward.RewardWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.reward.application.service.MilestoneQueryService;
import metro.ExoticStamp.modules.reward.application.service.RewardCommandService;
import metro.ExoticStamp.modules.reward.application.service.UserRewardQueryService;
import metro.ExoticStamp.modules.reward.application.view.UserRewardView;
import metro.ExoticStamp.modules.reward.domain.exception.RedeemNotSupportedException;
import metro.ExoticStamp.modules.reward.domain.model.RewardStatus;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.presentation.mapper.RewardPresentationMapper;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserRewardController.class)
@Import({RewardWebMvcTestSecurityConfig.class, RewardPresentationMapper.class})
class UserRewardControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private UserRewardQueryService userRewardQueryService;
    @MockBean private MilestoneQueryService milestoneQueryService;
    @MockBean private RewardCommandService rewardCommandService;
    @MockBean private JwtProvider jwtProvider;
    @MockBean private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private RoleQueryService roleQueryService;
    @MockBean private CustomAuthEntryPoint authEntryPoint;
    @MockBean private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    void unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/rewards/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001")
    void meAlias_delegatesToMyRewards() throws Exception {
        when(userRewardQueryService.listMyRewards(any(), any(), eq(0), eq(0)))
                .thenReturn(metro.ExoticStamp.common.response.PageResponse.of(
                        java.util.List.of(), 0, 0, 0, 20));

        mockMvc.perform(get("/api/v1/rewards/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001")
    void redeem_returns410Gone() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new RedeemNotSupportedException("disabled")).when(rewardCommandService).redeemVoucher(any(), eq(id));

        mockMvc.perform(post("/api/v1/rewards/{id}/redeem", id).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isGone())
                .andExpect(jsonPath("$.code").value("REDEEM_NOT_SUPPORTED"));
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001")
    void myRewardDetail_pendingStockNoVoucher() throws Exception {
        UUID id = UUID.randomUUID();
        when(userRewardQueryService.getMyReward(any(), eq(id))).thenReturn(UserRewardView.builder()
                .id(id)
                .userId(UUID.fromString("00000000-0000-0000-0000-000000000001"))
                .campaignId(UUID.randomUUID())
                .milestoneId(UUID.randomUUID())
                .milestoneCode("M1")
                .rewardType(RewardType.VOUCHER)
                .rewardTitle("V")
                .issuedAt(LocalDateTime.now())
                .status(RewardStatus.PENDING_STOCK)
                .voucher(null)
                .build());

        mockMvc.perform(get("/api/v1/rewards/my/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("PENDING_STOCK"))
                .andExpect(jsonPath("$.data.voucher").doesNotExist());
    }
}
