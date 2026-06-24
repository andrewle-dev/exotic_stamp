package metro.ExoticStamp.modules.user.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import metro.ExoticStamp.common.exceptions.GlobalExceptionHandler;
import metro.ExoticStamp.config.TestMethodSecurityConfig;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import metro.ExoticStamp.modules.user.application.UserCommandService;
import metro.ExoticStamp.modules.user.application.UserQueryService;
import metro.ExoticStamp.modules.user.application.view.UserView;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import({TestMethodSecurityConfig.class, GlobalExceptionHandler.class})
class UserControllerSecurityTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID OTHER_USER_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserCommandService commandService;

    @MockBean
    private UserQueryService queryService;

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

    @MockBean
    private RoleQueryService roleQueryService;

    @Test
    @WithMockUser(username = "user1")
    void getMe_success_doesNotExposeSensitiveFields() throws Exception {
        when(queryService.getByUsername("user1")).thenReturn(UserView.builder()
                .id(USER_ID)
                .username("user1")
                .email("user1@test.com")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build());

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("user1"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.tokenVersion").doesNotExist())
                .andExpect(jsonPath("$.refreshToken").doesNotExist());
    }

    @Test
    void getMe_unauthenticated_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "11111111-1111-1111-1111-111111111111")
    void updateMe_success() throws Exception {
        when(commandService.updateUser(any())).thenReturn(UserView.builder()
                .id(USER_ID)
                .username("user1")
                .firstname("Updated")
                .email("user1@test.com")
                .status("ACTIVE")
                .build());

        mockMvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstname\":\"Updated\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname").value("Updated"));
    }

    @Test
    void updateMe_unauthenticated_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstname\":\"Updated\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user1")
    void getById_nonAdmin_returns403() throws Exception {
        mockMvc.perform(get("/api/v1/users/" + OTHER_USER_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "MANAGE_USER"})
    void getById_admin_returns200() throws Exception {
        when(queryService.getById(OTHER_USER_ID)).thenReturn(UserView.builder()
                .id(OTHER_USER_ID)
                .username("other")
                .email("other@test.com")
                .status("ACTIVE")
                .build());

        mockMvc.perform(get("/api/v1/users/" + OTHER_USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("other"));
    }

    @Test
    @WithMockUser(username = "user1")
    void create_nonAdmin_returns403() throws Exception {
        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstname": "A",
                                  "lastname": "B",
                                  "username": "newuser",
                                  "email": "new@test.com",
                                  "phoneNumber": "+10000000001",
                                  "password": "Secret123!",
                                  "gender": false
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "MANAGE_USER"})
    void create_admin_returns201() throws Exception {
        when(commandService.createUser(any())).thenReturn(UserView.builder()
                .id(OTHER_USER_ID)
                .username("newuser")
                .email("new@test.com")
                .status("ACTIVE")
                .build());

        mockMvc.perform(post("/api/v1/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "firstname": "A",
                                  "lastname": "B",
                                  "username": "newuser",
                                  "email": "new@test.com",
                                  "phoneNumber": "+10000000001",
                                  "password": "Secret123!",
                                  "gender": false
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "user1")
    void updateById_nonAdmin_returns403() throws Exception {
        mockMvc.perform(put("/api/v1/users/" + OTHER_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstname\":\"Hacked\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "MANAGE_USER"})
    void updateById_admin_returns200() throws Exception {
        when(commandService.updateUser(any())).thenReturn(UserView.builder()
                .id(OTHER_USER_ID)
                .username("other")
                .firstname("AdminUpdated")
                .build());

        mockMvc.perform(put("/api/v1/users/" + OTHER_USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstname\":\"AdminUpdated\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user1")
    void delete_nonAdmin_returns403() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + OTHER_USER_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "MANAGE_USER"})
    void delete_admin_returns204() throws Exception {
        mockMvc.perform(delete("/api/v1/users/" + OTHER_USER_ID))
                .andExpect(status().isNoContent());
    }
}
