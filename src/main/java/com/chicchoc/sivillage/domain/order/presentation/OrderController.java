package com.chicchoc.sivillage.domain.order.presentation;

import com.chicchoc.sivillage.domain.cart.application.CartService;
import com.chicchoc.sivillage.domain.order.application.OrderService;
import com.chicchoc.sivillage.domain.order.dto.out.OrderDetailResponseDto;
import com.chicchoc.sivillage.domain.order.dto.out.OrderResponseDto;
import com.chicchoc.sivillage.domain.order.vo.in.CartUuidRequestVo;
import com.chicchoc.sivillage.domain.order.vo.in.OrderProductRequestVo;
import com.chicchoc.sivillage.domain.order.vo.in.OrderRequestVo;
import com.chicchoc.sivillage.domain.order.vo.out.OrderDetailResponseVo;
import com.chicchoc.sivillage.domain.order.vo.out.OrderResponseVo;
import com.chicchoc.sivillage.global.common.entity.BaseResponse;
import com.chicchoc.sivillage.global.common.entity.BaseResponseStatus;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/order")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;

    @Operation(summary = "createOrder API", description = "주문 생성", tags = {"주문"})
    @PostMapping
    public BaseResponse<Void> createOrder(@AuthenticationPrincipal UserDetails userDetails,
                                          @RequestBody OrderRequestVo orderRequestVo) {

        orderService.createOrder(
                orderRequestVo.toDto(userDetails.getUsername()),
                orderRequestVo.getOrderProductRequestVoList().stream()
                        .map(OrderProductRequestVo::toDto)
                        .toList(),
                orderRequestVo.getCartUuidRequestVoList().stream()
                        .map(CartUuidRequestVo::toDto).toList());

        return new BaseResponse<>(BaseResponseStatus.SUCCESS);
    }

    @Operation(summary = "getOrders API", description = "주문 조회", tags = {"주문"})
    @GetMapping
    public BaseResponse<List<OrderResponseVo>> getOrders(@AuthenticationPrincipal UserDetails userDetails,
                                                         @RequestParam("startDate") String startDate,
                                                         @RequestParam("endDate") String endDate) {

        return new BaseResponse<>(orderService.getOrder(userDetails.getUsername(), startDate,
                        endDate).stream()
                .map(OrderResponseDto::toVo)
                .toList());
    }

    @Operation(summary = "getOrderDetails API", description = "주문 상세 조회", tags = {"주문"})
    @GetMapping("/{orderUuid}")
    public BaseResponse<OrderDetailResponseVo> getOrderDetails(Authentication authentication,
                                                               @PathVariable("orderUuid") String orderUuid) {
        OrderDetailResponseDto orderDetailResponseDto = orderService.getOrderDetail(orderUuid);
        OrderDetailResponseVo orderDetailResponseVo = orderDetailResponseDto.toVo();
        return new BaseResponse<>(orderDetailResponseVo);
    }

    @Operation(summary = "deleteOrder API", description = "주문 삭제", tags = {"주문"})
    @DeleteMapping("/{orderUuid}")
    public BaseResponse<Void> deleteOrder(@PathVariable("orderUuid") String orderUuid) {
        orderService.deleteOrder(orderUuid);
        return new BaseResponse<>();
    }
}
