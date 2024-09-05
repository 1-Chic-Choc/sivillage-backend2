package com.chicchoc.sivillage.domain.cart.dto.in;

import com.chicchoc.sivillage.domain.cart.domain.Cart;
import lombok.Getter;

@Getter
public class CartRequestDto {

    private String cartName;

    public Cart toEntity(String cartUuid, boolean isSigned, String userUuid) {
        return Cart.builder()
                .cartUuid(cartUuid)
                .isSigned(isSigned)
                .userUuid(userUuid)
                .cartName(cartName)
                .build();
    }
}
