package com.chicchoc.sivillage.domain.cart.dto.in;

import com.chicchoc.sivillage.domain.cart.domain.Cart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartRequestDto {

    private String productOptionUuid;
    private int amount;

    public Cart toEntity(String userUuid) {
        return Cart.builder()
                .userUuid(userUuid)
                .productOptionUuid(productOptionUuid)
                .amount(amount)
                .isSelected(false)
                .build();
    }
}
