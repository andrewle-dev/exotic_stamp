package metro.ExoticStamp.modules.metro.presentation;

import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.metro.MetroWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.metro.application.LineCommandService;
import metro.ExoticStamp.modules.metro.application.LineQueryService;
import metro.ExoticStamp.modules.metro.application.view.LineView;
import metro.ExoticStamp.common.reorder.ReorderItemView;
import metro.ExoticStamp.common.reorder.ReorderResultView;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminMetroLineController.class)
@Import({MetroWebMvcTestSecurityConfig.class, MetroPresentationMapper.class})
class AdminMetroLineControllerTest {

    private static final UUID LINE_ID = UUID.fromString("00000000-0000-0000-0000-000000000101");

    @Autowired private MockMvc mockMvc;
    @MockBean private LineQueryService lineQueryService;
    @MockBean private LineCommandService lineCommandService;
    @MockBean private JwtProvider jwtProvider;
    @MockBean private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private RoleQueryService roleQueryService;
    @MockBean private CustomAuthEntryPoint authEntryPoint;
    @MockBean private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "METRO_LINE_MANAGE"})
    void createLine_created() throws Exception {
        when(lineCommandService.createLine(any())).thenReturn(
                LineView.builder().id(LINE_ID).code("L1").name("Line").status("ACTIVE").build());

        mockMvc.perform(post("/api/v1/admin/metro/lines").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"L1\",\"name\":\"Line\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.code").value("L1"));
    }

    @Test
    void createLine_unauthenticated_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/admin/metro/lines").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"L1\",\"name\":\"Line\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void createLine_nonAdmin_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/admin/metro/lines").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"L1\",\"name\":\"Line\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "METRO_LINE_MANAGE"})
    void reorderLines_ok() throws Exception {
        when(lineCommandService.reorderLines(any())).thenReturn(
                new ReorderResultView(null, 1, List.of(new ReorderItemView(LINE_ID, 0))));

        mockMvc.perform(patch("/api/v1/admin/metro/lines/reorder").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"orderedIds\":[\"" + LINE_ID + "\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.updatedCount").value(1))
                .andExpect(jsonPath("$.data.items[0].id").value(LINE_ID.toString()))
                .andExpect(jsonPath("$.data.items[0].sortOrder").value(0));
    }
}
