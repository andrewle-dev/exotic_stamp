package metro.ExoticStamp.modules.metro.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.metro.application.PublicAssetUploadService;
import metro.ExoticStamp.modules.metro.presentation.dto.response.PublicAssetUploadResponse;
import metro.ExoticStamp.modules.metro.presentation.mapper.MetroPresentationMapper;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/uploads")
@RequiredArgsConstructor
@Tag(name = "Admin Uploads")
public class AdminPublicUploadController {

    private final PublicAssetUploadService publicAssetUploadService;
    private final MetroPresentationMapper presentationMapper;

    @PostMapping(value = "/public", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('UPLOAD_PUBLIC_ASSET')")
    @Operation(summary = "Upload public asset", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PublicAssetUploadResponse>> uploadPublic(
            @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toResponse(publicAssetUploadService.uploadPublicAsset(file))));
    }
}
