package com.chicchoc.sivillage.domain.product.application;

import java.util.List;

public interface ProductLikeService {

    void toggleProductLike(String productUuid, String userUuid);

    Boolean isLikedProduct(String productUuid, String userUuid);

    List<String> getLikedProductList(String userUuid);
}
