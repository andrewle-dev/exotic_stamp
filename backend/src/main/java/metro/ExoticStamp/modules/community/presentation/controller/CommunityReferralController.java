package metro.ExoticStamp.modules.community.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.common.security.SecurityPrincipalSupport;
import metro.ExoticStamp.modules.community.application.service.ReferralCodeQueryService;
import metro.ExoticStamp.modules.community.application.service.ReferralCommandService;
import metro.ExoticStamp.modules.community.application.service.ReferralQueryService;
import metro.ExoticStamp.modules.community.presentation.mapper.CommunityPresentationMapper;
import metro.ExoticStamp.modules.community.presentation.request.ApplyReferralRequest;
import metro.ExoticStamp.modules.community.presentation.response.MyReferralsResponse;
import metro.ExoticStamp.modules.community.presentation.response.ReferralCodeResponse;
import metro.ExoticStamp.modules.community.presentation.response.ReferralResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/community")
@RequiredArgsConstructor
@Tag(name = "Community")
public class CommunityReferralController {

    private final ReferralCodeQueryService referralCodeQueryService;
    private final ReferralCommandService referralCommandService;
    private final ReferralQueryService referralQueryService;
    private final CommunityPresentationMapper presentationMapper;

    @GetMapping("/referral-code")
    @Operation(summary = "Get my referral code", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ReferralCodeResponse>> getMyReferralCode(
            @AuthenticationPrincipal UserDetails principal
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toReferralCodeResponse(referralCodeQueryService.getOrCreateMyReferralCode(userId))));
    }

    @PostMapping("/referrals/apply")
    @Operation(summary = "Apply a referral code", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<ReferralResponse>> applyReferral(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody ApplyReferralRequest request
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(
                presentationMapper.toReferralResponse(
                        referralCommandService.applyReferral(userId, presentationMapper.toApplyReferralCommand(request)))));
    }

    @GetMapping("/referrals/me")
    @Operation(summary = "My referral status", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<ApiResponse<MyReferralsResponse>> getMyReferrals(
            @AuthenticationPrincipal UserDetails principal
    ) {
        UUID userId = SecurityPrincipalSupport.requireUserId(principal);
        return ResponseEntity.ok(ApiResponse.ok(
                presentationMapper.toMyReferralsResponse(referralQueryService.getMyReferrals(userId))));
    }
}
