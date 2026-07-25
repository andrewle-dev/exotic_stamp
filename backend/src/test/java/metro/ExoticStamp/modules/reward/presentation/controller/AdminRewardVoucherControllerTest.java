package metro.ExoticStamp.modules.reward.presentation.controller;

import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.modules.reward.RewardWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.reward.application.service.VoucherPoolCommandService;
import metro.ExoticStamp.modules.reward.application.service.VoucherPoolQueryService;
import metro.ExoticStamp.modules.reward.application.view.VoucherPoolView;
import metro.ExoticStamp.modules.reward.domain.model.VoucherPoolStatus;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminRewardVoucherController.class)
@Import({RewardWebMvcTestSecurityConfig.class, RewardPresentationMapper.class})
class AdminRewardVoucherControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private VoucherPoolQueryService voucherPoolQueryService;
    @MockBean private VoucherPoolCommandService voucherPoolCommandService;
    @MockBean private JwtProvider jwtProvider;
    @MockBean private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private RoleQueryService roleQueryService;
    @MockBean private CustomAuthEntryPoint authEntryPoint;
    @MockBean private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "VOUCHER_POOL_MANAGE"})
    void importVouchers_adminAllowed() throws Exception {
        UUID milestoneId = UUID.randomUUID();
        when(voucherPoolCommandService.importVouchers(any())).thenReturn(2);

        mockMvc.perform(post("/api/v1/admin/rewards/vouchers/import").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"milestoneId":"%s","codes":["A","B"]}
                                """.formatted(milestoneId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.importedCount").value(2));
    }

    @Test
    @WithMockUser(roles = "USER")
    void importVouchers_userDenied() throws Exception {
        mockMvc.perform(post("/api/v1/admin/rewards/vouchers/import").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"milestoneId":"%s","codes":["A"]}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "VOUCHER_POOL_MANAGE"})
    void listVouchers_returnsPage() throws Exception {
        UUID id = UUID.randomUUID();
        UUID milestoneId = UUID.randomUUID();
        when(voucherPoolQueryService.list(eq(milestoneId), eq("AVAILABLE"), eq(0), eq(0)))
                .thenReturn(PageResponse.of(
                        List.of(VoucherPoolView.builder()
                                .id(id)
                                .milestoneId(milestoneId)
                                .code("CODE1")
                                .status(VoucherPoolStatus.AVAILABLE)
                                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                                .build()),
                        1, 1, 0, 20));

        mockMvc.perform(get("/api/v1/admin/rewards/vouchers")
                        .param("milestoneId", milestoneId.toString())
                        .param("status", "AVAILABLE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].code").value("CODE1"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "VOUCHER_POOL_MANAGE"})
    void disableVoucher_ok() throws Exception {
        UUID id = UUID.randomUUID();
        when(voucherPoolCommandService.disable(id)).thenReturn(VoucherPoolView.builder()
                .id(id)
                .code("X")
                .status(VoucherPoolStatus.DISABLED)
                .createdAt(LocalDateTime.now())
                .build());

        mockMvc.perform(patch("/api/v1/admin/rewards/vouchers/" + id).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("DISABLED"));
    }
}
