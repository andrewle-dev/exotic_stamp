package metro.ExoticStamp.modules.reward.presentation.controller;

import metro.ExoticStamp.common.exceptions.GlobalExceptionHandler;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.modules.rbac.application.support.RbacSecurityContextHelper;
import metro.ExoticStamp.modules.reward.RewardWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.reward.application.service.AdminRewardCommandService;
import metro.ExoticStamp.modules.reward.application.service.AdminRewardQueryService;
import metro.ExoticStamp.modules.reward.application.service.RewardReconcileService;
import metro.ExoticStamp.modules.reward.application.view.RewardView;
import metro.ExoticStamp.modules.reward.domain.model.RewardType;
import metro.ExoticStamp.modules.reward.presentation.mapper.RewardPresentationMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminRewardController.class)
@Import({RewardWebMvcTestSecurityConfig.class, RewardPresentationMapper.class, GlobalExceptionHandler.class})
class AdminRewardControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private AdminRewardQueryService adminRewardQueryService;
    @MockBean private AdminRewardCommandService adminRewardCommandService;
    @MockBean private RewardReconcileService rewardReconcileService;
    @MockBean private RbacSecurityContextHelper securityContextHelper;
    @MockBean private JwtProvider jwtProvider;
    @MockBean private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private RoleQueryService roleQueryService;
    @MockBean private CustomAuthEntryPoint authEntryPoint;
    @MockBean private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    void listRewards_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/v1/admin/rewards"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void listRewards_userDenied() throws Exception {
        mockMvc.perform(get("/api/v1/admin/rewards"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void listRewards_adminOk() throws Exception {
        UUID rewardId = UUID.randomUUID();
        when(adminRewardQueryService.listRewards(null, 0, 0)).thenReturn(PageResponse.of(
                List.of(RewardView.builder()
                        .id(rewardId)
                        .milestoneId(UUID.randomUUID())
                        .rewardType(RewardType.VOUCHER)
                        .name("Coffee voucher")
                        .issuedCount(0)
                        .active(true)
                        .build()),
                1, 1, 0, 20));

        mockMvc.perform(get("/api/v1/admin/rewards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content[0].name").value("Coffee voucher"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void getReward_adminOk() throws Exception {
        UUID id = UUID.randomUUID();
        when(adminRewardQueryService.getReward(id)).thenReturn(RewardView.builder()
                .id(id)
                .milestoneId(UUID.randomUUID())
                .rewardType(RewardType.DIGITAL_STICKER)
                .name("Sticker")
                .issuedCount(2)
                .active(true)
                .build());

        mockMvc.perform(get("/api/v1/admin/rewards/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(id.toString()))
                .andExpect(jsonPath("$.data.rewardType").value("DIGITAL_STICKER"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createReward_adminCreated() throws Exception {
        UUID milestoneId = UUID.randomUUID();
        UUID rewardId = UUID.randomUUID();
        when(adminRewardCommandService.createReward(any())).thenReturn(RewardView.builder()
                .id(rewardId)
                .milestoneId(milestoneId)
                .rewardType(RewardType.VOUCHER)
                .name("Voucher pack")
                .valueAmount(new BigDecimal("10.00"))
                .issuedCount(0)
                .active(true)
                .build());

        mockMvc.perform(post("/api/v1/admin/rewards").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"milestoneId":"%s","rewardType":"VOUCHER","name":"Voucher pack","valueAmount":10.00}
                                """.formatted(milestoneId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Voucher pack"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reconcile_adminSuccess() throws Exception {
        UUID adminId = UUID.randomUUID();
        when(securityContextHelper.currentUserId()).thenReturn(Optional.of(adminId));
        when(rewardReconcileService.runReconcile(any())).thenReturn(
                new RewardReconcileService.ReconcileResult(3, 2, 1, 1, 0, 0, false, null, adminId));

        mockMvc.perform(post("/api/v1/admin/rewards/reconcile").with(csrf())
                        .param("batchSize", "25")
                        .param("dryRun", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.missingExamined").value(3))
                .andExpect(jsonPath("$.data.missingRepaired").value(2))
                .andExpect(jsonPath("$.data.pendingFulfilled").value(1))
                .andExpect(jsonPath("$.data.initiatedByAdminId").value(adminId.toString()))
                .andExpect(jsonPath("$.data.dryRun").value(false));

        ArgumentCaptor<RewardReconcileService.ReconcileRequest> captor =
                ArgumentCaptor.forClass(RewardReconcileService.ReconcileRequest.class);
        verify(rewardReconcileService).runReconcile(captor.capture());
        assertEquals(25, captor.getValue().batchSize());
        assertFalse(captor.getValue().dryRun());
        assertEquals(adminId, captor.getValue().initiatedByAdminId());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reconcile_dryRunReturnsSkipReason() throws Exception {
        UUID adminId = UUID.randomUUID();
        when(securityContextHelper.currentUserId()).thenReturn(Optional.of(adminId));
        when(rewardReconcileService.runReconcile(any())).thenReturn(
                new RewardReconcileService.ReconcileResult(4, 0, 2, 0, 0, 0, true, "dry-run", adminId));

        mockMvc.perform(post("/api/v1/admin/rewards/reconcile").with(csrf())
                        .param("dryRun", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.skipped").value(true))
                .andExpect(jsonPath("$.data.skipReason").value("dry-run"))
                .andExpect(jsonPath("$.data.dryRun").value(true));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reconcile_busyReturns409() throws Exception {
        when(securityContextHelper.currentUserId()).thenReturn(Optional.of(UUID.randomUUID()));
        when(rewardReconcileService.runReconcile(any())).thenReturn(
                new RewardReconcileService.ReconcileResult(0, 0, 0, 0, 0, 0, true, "already-running", null));

        mockMvc.perform(post("/api/v1/admin/rewards/reconcile").with(csrf()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RECONCILE_BUSY"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void reconcile_userDenied() throws Exception {
        mockMvc.perform(post("/api/v1/admin/rewards/reconcile").with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void reconcile_responseDoesNotExposeVoucherCodes() throws Exception {
        when(securityContextHelper.currentUserId()).thenReturn(Optional.of(UUID.randomUUID()));
        when(rewardReconcileService.runReconcile(any())).thenReturn(
                new RewardReconcileService.ReconcileResult(1, 1, 1, 1, 0, 0, false, null, UUID.randomUUID()));

        mockMvc.perform(post("/api/v1/admin/rewards/reconcile").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.code").doesNotExist())
                .andExpect(jsonPath("$.data.voucherCode").doesNotExist())
                .andExpect(jsonPath("$.data.voucher").doesNotExist());
    }
}
