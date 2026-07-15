package metro.ExoticStamp.modules.reward.presentation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import metro.ExoticStamp.common.response.ApiResponse;
import metro.ExoticStamp.modules.reward.application.service.PartnerPromotionalQueryService;
import metro.ExoticStamp.modules.reward.presentation.mapper.RewardPresentationMapper;
import metro.ExoticStamp.modules.reward.presentation.response.PromotionalPartnerBannerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
@Tag(name = "Partners", description = "Public partner promotional APIs")
public class PartnerPublicController {

    private final PartnerPromotionalQueryService partnerPromotionalQueryService;
    private final RewardPresentationMapper presentationMapper;

    @GetMapping("/promotional-banners")
    @Operation(summary = "List eligible partner banners for mobile Home carousel")
    public ResponseEntity<ApiResponse<List<PromotionalPartnerBannerResponse>>> listPromotionalBanners() {
        List<PromotionalPartnerBannerResponse> banners = partnerPromotionalQueryService.listPromotionalBanners()
                .stream()
                .map(presentationMapper::toPromotionalPartnerBannerResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(banners));
    }
}
