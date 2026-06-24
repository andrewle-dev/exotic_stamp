package metro.ExoticStamp.modules.reward.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.common.response.PageResponse;
import metro.ExoticStamp.common.security.SecurityPrincipalSupport;
import metro.ExoticStamp.modules.reward.application.service.MilestoneQueryService;
import metro.ExoticStamp.modules.reward.application.service.RewardCommandService;
import metro.ExoticStamp.modules.reward.application.service.UserRewardQueryService;
import metro.ExoticStamp.modules.reward.presentation.mapper.RewardPresentationMapper;
import metro.ExoticStamp.modules.reward.presentation.request.RedeemRewardRequest;
import metro.ExoticStamp.modules.reward.presentation.response.MilestoneResponse;
import metro.ExoticStamp.modules.reward.presentation.response.UserRewardResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/rewards")
@RequiredArgsConstructor
@Tag(name = "Rewards")
public class UserRewardController {

    private final UserRewardQueryService userRewardQueryService;
    private final RewardCommandService rewardCommandService;
    private final MilestoneQueryService milestoneQueryService;
    private final RewardPresentationMapper presentationMapper;

    @GetMapping("/my")
    @Operation(summary = "List my rewards (paginated)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<UserRewardResponse>>> myRewards(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        return listMyRewards(principal, status, page, size);
    }

    @GetMapping("/me")
    @Operation(summary = "List my rewards (alias of /my)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<UserRewardResponse>>> meRewards(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        return listMyRewards(principal, status, page, size);
    }

    private ResponseEntity<ApiResponse<PageResponse<UserRewardResponse>>> listMyRewards(
            UserDetails principal,
            String status,
            int page,
            Integer size
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        int s = size != null ? size : 0;
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toUserRewardListPage(
                        userRewardQueryService.listMyRewards(userId, presentationMapper.parseRewardStatus(status), page, s))));
    }

    @GetMapping("/my/{id}")
    @Operation(summary = "Get my reward detail", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserRewardResponse>> getMyReward(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id
    ) {
        return getMyRewardDetail(principal, id);
    }

    @GetMapping("/me/{id}")
    @Operation(summary = "Get my reward detail (alias of /my/{id})", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserRewardResponse>> getMeReward(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id
    ) {
        return getMyRewardDetail(principal, id);
    }

    private ResponseEntity<ApiResponse<UserRewardResponse>> getMyRewardDetail(
            UserDetails principal,
            UUID id
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toUserRewardDetail(userRewardQueryService.getMyReward(userId, id))));
    }

    @GetMapping("/milestones")
    @Operation(summary = "Active reward milestones for a campaign", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<PageResponse<MilestoneResponse>>> activeMilestones(
            @RequestParam UUID campaignId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) Integer size
    ) {
        int s = size != null ? size : 0;
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toMilestonePage(
                        milestoneQueryService.list(campaignId, "ACTIVE", page, s))));
    }

    @GetMapping("/{id}")
    @Deprecated
    @Operation(summary = "Get reward detail (deprecated; use /my/{id})", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserRewardResponse>> getByIdLegacy(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id
    ) {
        return getMyReward(principal, id);
    }

    @PostMapping("/{id}/redeem")
    @Deprecated
    @Operation(summary = "Redeem voucher reward (disabled in MVP — returns 410)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<UserRewardResponse>> redeem(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody(required = false) RedeemRewardRequest ignored
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        rewardCommandService.redeemVoucher(userId, id);
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toUserRewardDetail(userRewardQueryService.getMyReward(userId, id))));
    }
}
