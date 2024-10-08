package com.chicchoc.sivillage.domain.promotion.presentation;

import com.chicchoc.sivillage.domain.promotion.application.PromotionLikeService;
import com.chicchoc.sivillage.global.common.aop.annotation.CheckAuthentication;
import com.chicchoc.sivillage.global.common.entity.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "프로모션 좋아요", description = "프로모션 좋아요 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/promotion/like")
public class PromotionLikeController {

    private final PromotionLikeService promotionLikeService;

    @CheckAuthentication
    @Operation(summary = "프로모션 좋아요", description = "@return : Void", tags = {"프로모션 좋아요"})
    @PostMapping("/{promotionUuid}")
    public BaseResponse<Void> likePromotion(@PathVariable String promotionUuid, Authentication authentication) {

        promotionLikeService.saveAndDeletePromotionLike(promotionUuid, authentication.getName());

        return new BaseResponse<>();
    }

    @CheckAuthentication
    @Operation(summary = "프로모션 단건 좋아요 여부 조회",
            description = "@return : Boolean isLiked",
            tags = {"프로모션 좋아요"})
    @GetMapping("/{promotionUuid}")
    public BaseResponse<Boolean> isLikedPromotion(@PathVariable String promotionUuid, Authentication authentication) {

        boolean isLiked = promotionLikeService.isLikedPromotion(promotionUuid, authentication.getName());

        return new BaseResponse<>(isLiked);
    }

    @CheckAuthentication
    @Operation(summary = "내가 좋아요한 프로모션 전체 조회",
            description = "@return : List<String promotionUuid>, 최근 좋아요한 순으로 정렬",
            tags = {"프로모션 좋아요"})
    @GetMapping("/all")
    public BaseResponse<List<String>> isLikedPromotion(Authentication authentication) {

        List<String> likedPromotionList = promotionLikeService.getLikedPromotionList(authentication.getName());

        return new BaseResponse<>(likedPromotionList);
    }
}
