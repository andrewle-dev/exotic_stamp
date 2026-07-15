package metro.ExoticStamp.modules.metro.presentation;

import metro.ExoticStamp.common.exceptions.storage.InvalidImageTypeException;
import metro.ExoticStamp.modules.auth.infrastructure.jwt.JwtProvider;
import metro.ExoticStamp.modules.auth.infrastructure.security.AccessTokenRevocationValidator;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAccessDeniedHandler;
import metro.ExoticStamp.modules.auth.infrastructure.security.CustomAuthEntryPoint;
import metro.ExoticStamp.modules.auth.infrastructure.security.UserDetailsServiceImpl;
import metro.ExoticStamp.modules.metro.MetroWebMvcTestSecurityConfig;
import metro.ExoticStamp.modules.metro.application.PublicAssetUploadService;
import metro.ExoticStamp.modules.metro.application.view.PublicAssetUploadView;
import metro.ExoticStamp.modules.metro.presentation.mapper.MetroPresentationMapper;
import metro.ExoticStamp.modules.rbac.application.RoleQueryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminPublicUploadController.class)
@Import({MetroWebMvcTestSecurityConfig.class, MetroPresentationMapper.class})
class AdminPublicUploadControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockBean private PublicAssetUploadService publicAssetUploadService;
    @MockBean private JwtProvider jwtProvider;
    @MockBean private AccessTokenRevocationValidator accessTokenRevocationValidator;
    @MockBean private UserDetailsServiceImpl userDetailsService;
    @MockBean private RoleQueryService roleQueryService;
    @MockBean private CustomAuthEntryPoint authEntryPoint;
    @MockBean private CustomAccessDeniedHandler accessDeniedHandler;

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "UPLOAD_PUBLIC_ASSET"})
    void uploadPng_success() throws Exception {
        when(publicAssetUploadService.uploadPublicAsset(any())).thenReturn(
                PublicAssetUploadView.builder().url("http://localhost:8080/uploads/public/abc.png").build());

        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[10]);
        mockMvc.perform(multipart("/api/v1/admin/uploads/public").file(file).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.url").value("http://localhost:8080/uploads/public/abc.png"));
    }

    @Test
    @WithMockUser(authorities = {"ROLE_ADMIN", "UPLOAD_PUBLIC_ASSET"})
    void uploadSvg_rejected() throws Exception {
        when(publicAssetUploadService.uploadPublicAsset(any()))
                .thenThrow(new InvalidImageTypeException("image/svg+xml"));

        MockMultipartFile file = new MockMultipartFile("file", "a.svg", "image/svg+xml", new byte[10]);
        mockMvc.perform(multipart("/api/v1/admin/uploads/public").file(file).with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    void upload_unauthenticated_returns401() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[10]);
        mockMvc.perform(multipart("/api/v1/admin/uploads/public").file(file).with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "USER")
    void upload_nonAdmin_returns403() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[10]);
        mockMvc.perform(multipart("/api/v1/admin/uploads/public").file(file).with(csrf()))
                .andExpect(status().isForbidden());
    }
}
