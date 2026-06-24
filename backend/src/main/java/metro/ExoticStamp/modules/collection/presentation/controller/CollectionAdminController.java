package metro.ExoticStamp.modules.collection.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.collection.application.service.CollectionAdminQueryService;
import metro.ExoticStamp.modules.collection.application.view.CollectionAdminStatsView;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/collections")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin Collections", description = "Collection statistics")
@SecurityRequirement(name = "bearerAuth")
public class CollectionAdminController {

    private final CollectionAdminQueryService collectionAdminQueryService;

    @GetMapping("/stats")
    @Operation(summary = "Aggregate collection statistics")
    public ResponseEntity<ApiResponse<CollectionAdminStatsView>> stats() {
        return ResponseEntity.ok(ApiResponse.ok(collectionAdminQueryService.getStats()));
    }
}
