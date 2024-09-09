package com.chicchoc.sivillage.domain.cart.application;

import com.chicchoc.sivillage.domain.cart.dto.in.CartProductRequestDto;
import com.chicchoc.sivillage.domain.cart.dto.out.CartProductResponseDto;

import java.util.List;

public interface CartProductService {

    void createCartProduct(CartProductRequestDto cartProductRequestDto);

    List<CartProductResponseDto> getCartProductList(String cartUuid);
}
