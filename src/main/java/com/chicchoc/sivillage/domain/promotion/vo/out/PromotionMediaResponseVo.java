package com.chicchoc.sivillage.domain.promotion.vo.out;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PromotionMediaResponseVo {

    private Long mediaId;

    private int mediaOrder;
}
