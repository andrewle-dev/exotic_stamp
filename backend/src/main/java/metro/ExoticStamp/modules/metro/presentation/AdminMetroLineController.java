package metro.ExoticStamp.modules.metro.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.metro.application.LineCommandService;
import metro.ExoticStamp.modules.metro.application.LineQueryService;
import metro.ExoticStamp.modules.metro.presentation.dto.MetroStatusApi;
import metro.ExoticStamp.modules.metro.presentation.dto.request.CreateLineRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.request.UpdateLineRequest;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineDetailResponse;
import metro.ExoticStamp.modules.metro.presentation.dto.response.LineResponse;
import metro.ExoticStamp.modules.metro.presentation.mapper.MetroPresentationMapper;
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
@RequestMapping("/api/v1/admin/metro/lines")
@RequiredArgsConstructor
@Tag(name = "Admin Metro Lines")
public class AdminMetroLineController {

    private final LineQueryService lineQueryService;
    private final LineCommandService lineCommandService;
    private final MetroPresentationMapper presentationMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_LINE_MANAGE')")
    @Operation(summary = "List metro lines (admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<LineResponse>>> list(
            @RequestParam(required = false) MetroStatusApi status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size,
            @RequestParam(required = false) String sort
    ) {
        int s = size != null ? size : 20;
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toLinePage(lineQueryService.searchAdminLines(
                        status != null ? status.name() : null, search, page, s, sort))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_LINE_MANAGE')")
    @Operation(summary = "Get metro line detail (admin)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<LineDetailResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toResponse(lineQueryService.getAdminLineDetail(id))));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_LINE_MANAGE')")
    @Operation(summary = "Create metro line", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<LineResponse>> create(@Valid @RequestBody CreateLineRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                presentationMapper.toResponse(lineCommandService.createLine(
                        presentationMapper.toCreateLineCommand(request)))));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_LINE_MANAGE')")
    @Operation(summary = "Update metro line", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<LineResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateLineRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toResponse(lineCommandService.updateLine(
                        presentationMapper.toUpdateLineCommand(id, request)))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('METRO_LINE_MANAGE')")
    @Operation(summary = "Soft-delete metro line", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        lineCommandService.deleteLine(id);
        return ResponseEntity.noContent().build();
    }
}
