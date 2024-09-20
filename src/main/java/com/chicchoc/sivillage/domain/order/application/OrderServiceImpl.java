package com.chicchoc.sivillage.domain.order.application;

import com.chicchoc.sivillage.domain.order.domain.DeliveryStatus;
import com.chicchoc.sivillage.domain.order.domain.Order;
import com.chicchoc.sivillage.domain.order.domain.OrderProduct;
import com.chicchoc.sivillage.domain.order.domain.OrderStatus;
import com.chicchoc.sivillage.domain.order.dto.in.OrderProductRequestDto;
import com.chicchoc.sivillage.domain.order.dto.in.OrderRequestDto;
import com.chicchoc.sivillage.domain.order.dto.out.OrderDetailResponseDto;
import com.chicchoc.sivillage.domain.order.dto.out.OrderResponseDto;
import com.chicchoc.sivillage.domain.order.infrastructure.OrderProductRepository;
import com.chicchoc.sivillage.domain.order.infrastructure.OrderRepository;
import com.chicchoc.sivillage.global.common.generator.NanoIdGenerator;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderProductRepository orderProductRepository;

    @Override
    public void createOrder(OrderRequestDto orderRequestDto, List<OrderProductRequestDto> orderProductRequestDtoList) {


        // 주문 정보 저장
        String paymentUuid = NanoIdGenerator.generateNanoId();

        orderRepository.save(orderRequestDto.toEntity(paymentUuid, LocalDateTime.now()));

        // 주문 제품 정보 저장

        // 배송 그룹 처리 -> 동일 브랜드끼리 같은 운송장 번호와 택배사 부여
        Map<String, String> brandToTrackingMap = new HashMap<>();
        Map<String, String> brandToDeliveryCompanyMap = new HashMap<>();

        List<OrderProduct> orderProductEntities = new ArrayList<>();

        orderProductRequestDtoList.stream()
                .peek(productDto -> {
                    String brandName = productDto.getBrandName();

                    // 동일 브랜드에 대해 같은 운송장 번호 및 택배사 할당
                    brandToTrackingMap.computeIfAbsent(brandName, k -> NanoIdGenerator.generateNanoId());
                    brandToDeliveryCompanyMap.computeIfAbsent(brandName, k -> assignRandomDeliveryCompany());
                })
                .map(productDto -> productDto.toEntity(orderRequestDto.getOrderUuid(),
                        brandToDeliveryCompanyMap.get(productDto.getBrandName()),
                        brandToTrackingMap.get(productDto.getBrandName()),
                        DeliveryStatus.PREPARING_SHIPMENT))
                .forEach(orderProductEntities::add);

        orderProductRepository.saveAll(orderProductEntities);
    }

    @Override
    public List<OrderResponseDto> getOrder(String userUuid, String startDate, String endDate) {

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime start = LocalDateTime.parse(startDate, formatter);
        LocalDateTime end = LocalDateTime.parse(endDate, formatter);

        List<String> orderUuidList = orderRepository.findOrderUuidByDateRangeWithUserUuid(userUuid, start, end);

        List<OrderProduct> orderProductList = new ArrayList<>();

        for (String orderUuid : orderUuidList) {
            List<OrderProduct> productsForOrder = orderProductRepository.findByOrderUuid(orderUuid);
            orderProductList.addAll(productsForOrder);
        }

        return orderProductList.stream()
                .sorted(Comparator.comparing(OrderProduct::getCreatedAt).reversed())
                .map(orderProduct -> OrderResponseDto.builder()
                        .orderUuid(orderProduct.getOrderUuid())
                        .productUuid(orderProduct.getProductUuid())
                        .productName(orderProduct.getProductName())
                        .brandName(orderProduct.getBrandName())
                        .productPrice(orderProduct.getProductPrice())
                        .discountedPrice(orderProduct.getDiscountedPrice())
                        .colorValue(orderProduct.getColorValue())
                        .sizeName(orderProduct.getSizeName())
                        .productOption(orderProduct.getProductOption())
                        .deliveryStatus(orderProduct.getDeliveryStatus())
                        .amount(orderProduct.getAmount())
                        .thumbnailUrl(orderProduct.getThumbnailUrl())
                        .createdAt(orderProduct.getCreatedAt())
                        .updatedAt(orderProduct.getUpdatedAt())
                        .build())
                .toList();
    }

    @Override
    public OrderDetailResponseDto getOrderDetail(String orderUuid) {

        Order order = orderRepository.findByOrderUuid(orderUuid);

        return OrderDetailResponseDto.builder()
                .ordererName(order.getOrdererName())
                .ordererEmail(order.getOrdererEmail())
                .ordererPhone(order.getOrdererPhone())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .recipientAddress(order.getRecipientAddress())
                .deliveryName(order.getDeliveryName())
                .deliveryRequest(order.getDeliveryRequest())
                .build();
    }

    @Override
    public void deleteOrder(String orderUuid) {

        Order order = orderRepository.findByOrderUuid(orderUuid);

        Order modifiedOrder = Order.builder()
                .id(order.getId())
                .orderUuid(orderUuid)
                .userUuid(order.getUserUuid())
                .paymentUuid(order.getPaymentUuid())
                .orderedAt(order.getOrderedAt())
                .orderStatus(OrderStatus.CANCELLED)
                .ordererName(order.getOrdererName())
                .ordererEmail(order.getOrdererEmail())
                .ordererPhone(order.getOrdererPhone())
                .postalCode(order.getPostalCode())
                .recipientName(order.getRecipientName())
                .recipientPhone(order.getRecipientPhone())
                .recipientAddress(order.getRecipientAddress())
                .deliveryName(order.getDeliveryName())
                .deliveryRequest(order.getDeliveryRequest())
                .build();

        orderRepository.save(modifiedOrder);

        List<OrderProduct> orderProductList = orderProductRepository.findByOrderUuid(orderUuid);

        List<OrderProduct> modifiedOrderProductList = new ArrayList<>();

        for (OrderProduct orderProduct : orderProductList) {
            OrderProduct modifiedOrderProduct = OrderProduct.builder()
                    .id(orderProduct.getId())
                    .orderUuid(orderProduct.getOrderUuid())
                    .productUuid(orderProduct.getProductUuid())
                    .productName(orderProduct.getProductName())
                    .brandName(orderProduct.getBrandName())
                    .productPrice(orderProduct.getProductPrice())
                    .discountedPrice(orderProduct.getDiscountedPrice())
                    .colorValue(orderProduct.getColorValue())
                    .sizeName(orderProduct.getSizeName())
                    .productOption(orderProduct.getProductOption())
                    .deliveryStatus(DeliveryStatus.DELIVERY_FAILED)
                    .amount(orderProduct.getAmount())
                    .thumbnailUrl(orderProduct.getThumbnailUrl())
                    .deliveryCompany(orderProduct.getDeliveryCompany())
                    .trackingNumber(orderProduct.getTrackingNumber())
                    .build();

            modifiedOrderProductList.add(modifiedOrderProduct);
        }

        orderProductRepository.saveAll(modifiedOrderProductList);
    }

    private String assignRandomDeliveryCompany() {
        List<String> deliveryCompanies = Arrays.asList(
                "우체국택배", "CJ대한통운", "로젠택배", "한진택배", "롯데택배", "드림택배", "대신택배", "일양로지스"
        );
        Random random = new Random();
        int randomIndex = random.nextInt(deliveryCompanies.size());
        return deliveryCompanies.get(randomIndex);
    }

}
