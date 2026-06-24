package metro.ExoticStamp.modules.rbac.presentation;

import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.rbac.application.PermissionCommandService;
import metro.ExoticStamp.modules.rbac.application.RoleCommandService;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.modules.rbac.domain.model.Role;
import metro.ExoticStamp.modules.rbac.domain.model.RoleStatus;
import metro.ExoticStamp.config.TestMethodSecurityConfig;
import metro.ExoticStamp.modules.rbac.application.view.RoleView;
import metro.ExoticStamp.modules.rbac.presentation.mapper.RolePresentationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RoleController.class)
@Import({TestMethodSecurityConfig.class, RolePresentationMapper.class})
class RoleControllerSecurityTest {
    private static final UUID ADMIN_ROLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID MANAGER_ROLE_ID = UUID.fromString("00000000-0000-0000-0000-000000000099");

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleCommandService roleCommandService;

    @MockBean
    private RoleQueryService roleQueryService;

    @MockBean
    private PermissionCommandService permissionCommandService;

    @MockBean
    private JwtProvider jwtProvider;

    @MockBean
    private AccessTokenRevocationValidator accessTokenRevocationValidator;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private CustomAuthEntryPoint authEntryPoint;

    @MockBean
    private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void listRoles_withAdminRole_returns200() throws Exception {
        when(roleQueryService.getAllRoles()).thenReturn(List.of(
                new RoleView(ADMIN_ROLE_ID, "ADMIN", null, "ACTIVE", true)));
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isOk());
    }

    @Test
    void listRoles_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void listRoles_nonAdmin_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/roles"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void createRole_withoutRbacAdminAuthority_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/roles")
                        .contentType("application/json")
                        .content("{\"roleCode\":\"MANAGER\",\"description\":\"x\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void assignRole_success() throws Exception {
        mockMvc.perform(post("/api/v1/roles/assign")
                        .contentType("application/json")
                        .content("{\"userId\":\"11111111-1111-1111-1111-111111111111\",\"roleName\":\"USER\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void assignRole_nonAdmin_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/roles/assign")
                        .contentType("application/json")
                        .content("{\"userId\":\"11111111-1111-1111-1111-111111111111\",\"roleName\":\"USER\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void revokeRole_success() throws Exception {
        mockMvc.perform(post("/api/v1/roles/revoke")
                        .contentType("application/json")
                        .content("{\"userId\":\"11111111-1111-1111-1111-111111111111\",\"roleName\":\"USER\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "RBAC_ADMIN"})
    void createRole_withRbacAdminAuthority_returns200() throws Exception {
        when(roleCommandService.createRole(anyString(), any())).thenReturn(Role.builder()
                .id(MANAGER_ROLE_ID)
                .role("MANAGER")
                .status(RoleStatus.ACTIVE)
                .systemRole(false)
                .build());
        mockMvc.perform(post("/api/v1/roles")
                        .contentType("application/json")
                        .content("{\"roleCode\":\"MANAGER\",\"description\":\"x\"}"))
                .andExpect(status().isOk());
    }
}
