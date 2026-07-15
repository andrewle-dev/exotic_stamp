package metro.ExoticStamp.modules.reward.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.reorder.ReorderResponse;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.modules.reward.application.service.MilestoneCommandService;
import metro.ExoticStamp.modules.reward.application.service.MilestoneQueryService;
import metro.ExoticStamp.modules.reward.presentation.mapper.RewardPresentationMapper;
import metro.ExoticStamp.modules.reward.presentation.request.CreateMilestoneRequest;
import metro.ExoticStamp.modules.reward.presentation.request.ReorderMilestonesRequest;
import metro.ExoticStamp.modules.reward.presentation.request.UpdateMilestoneRequest;
import metro.ExoticStamp.modules.reward.presentation.response.MilestoneResponse;
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
@RequestMapping("/api/v1/admin/rewards/milestones")
@RequiredArgsConstructor
@Tag(name = "Admin Reward Milestones")
public class AdminRewardMilestoneController {

    private final MilestoneQueryService milestoneQueryService;
    private final MilestoneCommandService milestoneCommandService;
    private final RewardPresentationMapper presentationMapper;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('REWARD_MILESTONE_MANAGE')")
    @Operation(summary = "List milestones", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<MilestoneResponse>>> list(
            @RequestParam(required = false) UUID campaignId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        int s = size != null ? size : 0;
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toMilestonePage(milestoneQueryService.list(campaignId, status, page, s))));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('REWARD_MILESTONE_MANAGE')")
    @Operation(summary = "Get milestone", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<MilestoneResponse>> get(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toMilestoneResponse(milestoneQueryService.get(id))));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('REWARD_MILESTONE_MANAGE')")
    @Operation(summary = "Create milestone", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<MilestoneResponse>> create(@Valid @RequestBody CreateMilestoneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                presentationMapper.toMilestoneResponse(
                        milestoneCommandService.create(presentationMapper.toCreateMilestoneCommand(request)))));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('REWARD_MILESTONE_MANAGE')")
    @Operation(summary = "Update milestone", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<MilestoneResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMilestoneRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toMilestoneResponse(
                        milestoneCommandService.update(presentationMapper.toUpdateMilestoneCommand(id, request)))));
    }

    @PatchMapping("/reorder")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('REWARD_MILESTONE_MANAGE')")
    @Operation(summary = "Reorder milestones in a campaign",
            description = "Dense-renumbers all non-deleted milestones in the campaign to 0..n-1. "
                    + "orderedIds must be a permutation of every milestone id in that campaign.",
            security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ReorderResponse>> reorder(@Valid @RequestBody ReorderMilestonesRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(ReorderResponse.from(
                milestoneCommandService.reorder(presentationMapper.toReorderMilestonesCommand(request)))));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') and hasAuthority('REWARD_MILESTONE_MANAGE')")
    @Operation(summary = "Soft delete milestone", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        milestoneCommandService.softDelete(id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
