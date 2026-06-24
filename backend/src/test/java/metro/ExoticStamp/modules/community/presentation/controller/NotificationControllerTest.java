package metro.ExoticStamp.modules.community.presentation.controller;

import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.community.CommunityWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.community.application.service.NotificationCommandService;
import metro.ExoticStamp.modules.community.application.service.NotificationQueryService;
import metro.ExoticStamp.modules.community.application.view.NotificationView;
import metro.ExoticStamp.modules.community.domain.model.NotificationType;
import metro.ExoticStamp.modules.community.presentation.mapper.CommunityPresentationMapper;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@Import({CommunityWebMvcTestSecurityConfig.class, CommunityPresentationMapper.class})
class NotificationControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private NotificationQueryService notificationQueryService;
    @MockBean private NotificationCommandService notificationCommandService;
    @MockBean private JwtProvider jwtProvider;
    @MockBean private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private RoleQueryService roleQueryService;
    @MockBean private CustomAuthEntryPoint authEntryPoint;
    @MockBean private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    void unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001")
    void listNotifications_success() throws Exception {
        UUID id = UUID.randomUUID();
        when(notificationQueryService.listMyNotifications(any(), any(), eq(0), eq(0)))
                .thenReturn(PageResponse.of(
                        List.of(NotificationView.builder()
                                .id(id)
                                .type("SYSTEM")
                                .title("Hello")
                                .body("World")
                                .read(false)
                                .createdAt(LocalDateTime.now())
                                .build()),
                        1, 1, 0, 20));

        mockMvc.perform(get("/api/v1/notifications"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].title").value("Hello"));
    }

    @Test
    @WithMockUser(username = "00000000-0000-0000-0000-000000000001")
    void markRead_success() throws Exception {
        UUID id = UUID.randomUUID();
        when(notificationCommandService.markRead(any(), eq(id))).thenReturn(NotificationView.builder()
                .id(id)
                .type("REWARD")
                .title("Reward")
                .body("Earned")
                .read(true)
                .readAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build());

        mockMvc.perform(patch("/api/v1/notifications/{id}/read", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.read").value(true));
    }
}
