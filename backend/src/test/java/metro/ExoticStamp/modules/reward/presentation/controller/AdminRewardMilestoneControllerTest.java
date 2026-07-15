package metro.ExoticStamp.modules.reward.presentation.controller;

import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.common.reorder.ReorderItemView;
import metro.ExoticStamp.common.reorder.ReorderResultView;
import metro.ExoticStamp.modules.reward.RewardWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.reward.application.service.MilestoneCommandService;
import metro.ExoticStamp.modules.reward.application.service.MilestoneQueryService;
import metro.ExoticStamp.modules.reward.application.view.MilestoneView;
import metro.ExoticStamp.modules.reward.domain.model.MilestoneStatus;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminRewardMilestoneController.class)
@Import({RewardWebMvcTestSecurityConfig.class, RewardPresentationMapper.class})
class AdminRewardMilestoneControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private MilestoneCommandService milestoneCommandService;
    @MockBean private MilestoneQueryService milestoneQueryService;
    @MockBean private JwtProvider jwtProvider;
    @MockBean private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private RoleQueryService roleQueryService;
    @MockBean private CustomAuthEntryPoint authEntryPoint;
    @MockBean private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "REWARD_MILESTONE_MANAGE"})
    void createMilestone_adminAllowed() throws Exception {
        UUID campaignId = UUID.randomUUID();
        when(milestoneCommandService.create(any())).thenReturn(MilestoneView.builder()
                .id(UUID.randomUUID())
                .campaignId(campaignId)
                .code("M1")
                .requiredStampCount(3)
                .name("Three stamps")
                .rewardType(RewardType.DIGITAL_STICKER)
                .rewardTitle("Sticker")
                .status(MilestoneStatus.DRAFT)
                .sortOrder(0)
                .build());

        mockMvc.perform(post("/api/v1/admin/rewards/milestones").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campaignId":"%s","code":"M1","requiredStampCount":3,"name":"Three stamps",
                                "rewardType":"DIGITAL_STICKER","rewardTitle":"Sticker"}
                                """.formatted(campaignId)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createMilestone_userDenied() throws Exception {
        mockMvc.perform(post("/api/v1/admin/rewards/milestones").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"campaignId":"%s","code":"M1","requiredStampCount":3,"name":"Three stamps",
                                "rewardType":"DIGITAL_STICKER","rewardTitle":"Sticker"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "REWARD_MILESTONE_MANAGE"})
    void reorderMilestones_ok() throws Exception {
        UUID campaignId = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        when(milestoneCommandService.reorder(any())).thenReturn(
                new ReorderResultView(campaignId, 1, List.of(new ReorderItemView(milestoneId, 0))));

        mockMvc.perform(patch("/api/v1/admin/rewards/milestones/reorder").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"campaignId\":\"" + campaignId + "\",\"orderedIds\":[\"" + milestoneId + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.scopeId").value(campaignId.toString()))
                .andExpect(jsonPath("$.data.updatedCount").value(1));
    }
}
