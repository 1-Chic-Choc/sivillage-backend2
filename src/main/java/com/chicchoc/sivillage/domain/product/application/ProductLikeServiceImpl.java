package com.chicchoc.sivillage.domain.product.application;

import com.chicchoc.sivillage.domain.product.domain.ProductLike;
import com.chicchoc.sivillage.domain.product.infrastructure.ProductLikeRepository;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductLikeServiceImpl implements ProductLikeService {

    private final ProductLikeRepository productLikeRepository;

    /**
     * 1. 좋아요가 있는지 확인 2. 좋아요가 있으면 수정(상태 변경) 3. 좋아요가 없으면 저장 좋아요 데이터 활용을 위해 기존 데이터의 상태를 수정하고 새 값을 넣는 로직으로 결정
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveAndUpdateProductLike(String productUuid, String userUuid) {
        // 1. 좋아요가 있는지 확인
        ProductLike productLike =
                productLikeRepository.findTopByProductUuidAndUserUuidAndIsLiked(
                                productUuid,
                                userUuid,
                                true)
                        .orElse(null);

        // 2. 좋아요가 있으면 수정
        if (productLike != null) {
            productLike.update(!productLike.getIsLiked());
        } else {
            // 3. 좋아요가 없으면 저장
            productLikeRepository.save(new ProductLike(productUuid, userUuid, true));
        }
    }

    // 좋아요 여부 확인
    @Override
    public Boolean isLikedProduct(String productUuid, String userUuid) {

        ProductLike productLike = productLikeRepository.findTopByProductUuidAndUserUuidAndIsLiked(productUuid, userUuid,
                        true)
                .orElse(null);

        return productLike != null; // 좋아요가 있으면 true, 없으면 false
    }

    // 좋아요한 상품 전체 조회
    @Override
    public List<String> getLikedProductList(String userUuid) {

        List<ProductLike> likedProductList = productLikeRepository.findByUserUuidAndIsLiked(userUuid, true);

        // 최근 좋아요한 순으로 정렬
        Collections.reverse(likedProductList);

        return likedProductList.stream()
                .map(ProductLike::getProductUuid)
                .toList();
    }
}
