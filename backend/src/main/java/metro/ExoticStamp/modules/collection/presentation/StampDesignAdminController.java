package metro.ExoticStamp.modules.collection.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.collection.application.service.StampDesignCommandService;
import metro.ExoticStamp.modules.collection.application.service.StampDesignQueryService;
import metro.ExoticStamp.modules.collection.presentation.dto.request.CreateStampDesignRequest;
import metro.ExoticStamp.modules.collection.presentation.dto.request.UpdateStampDesignRequest;
import metro.ExoticStamp.modules.collection.presentation.dto.response.StampDesignResponse;
import metro.ExoticStamp.modules.collection.presentation.mapper.CampaignPresentationMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/stamp-designs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN') and hasAuthority('STAMP_DESIGN_MANAGE')")
@Tag(name = "Admin Stamp Designs", description = "Stamp design configuration")
@SecurityRequirement(name = "bearerAuth")
public class StampDesignAdminController {

    private final StampDesignCommandService stampDesignCommandService;
    private final StampDesignQueryService stampDesignQueryService;
    private final CampaignPresentationMapper mapper;

    @PostMapping
    @Operation(summary = "Create stamp design")
    public ResponseEntity<ApiResponse<StampDesignResponse>> create(@Valid @RequestBody CreateStampDesignRequest request) {
        var view = stampDesignCommandService.create(mapper.toCreateCommand(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(mapper.toResponse(view)));
    }

    @GetMapping
    @Operation(summary = "List stamp designs")
    public ResponseEntity<ApiResponse<PageResponse<StampDesignResponse>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ResponseEntity.ok(ApiResponse.ok(mapper.toStampDesignPage(stampDesignQueryService.list(page, size))));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get stamp design by id")
    public ResponseEntity<ApiResponse<StampDesignResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(mapper.toResponse(stampDesignQueryService.getById(id))));
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update stamp design")
    public ResponseEntity<ApiResponse<StampDesignResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStampDesignRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(
                mapper.toResponse(stampDesignCommandService.update(mapper.toUpdateCommand(id, request)))));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft-delete stamp design")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        stampDesignCommandService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
