package com.chicchoc.sivillage.domain.product.dto.in;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ProductPerBrandRequestDto {
    private Long brandId;
}
