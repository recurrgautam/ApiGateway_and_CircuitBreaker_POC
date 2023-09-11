package com.GatewayConfiguration.orderservice.service;

import com.GatewayConfiguration.orderservice.dto.InventoryResponse;
import com.GatewayConfiguration.orderservice.dto.OrderLineItemsDto;
import com.GatewayConfiguration.orderservice.dto.OrderRequest;
import com.GatewayConfiguration.orderservice.model.Order;
import com.GatewayConfiguration.orderservice.model.OrderLineItems;
import com.GatewayConfiguration.orderservice.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
@RequiredArgsConstructor
@Service
@Slf4j
public class OrderService {
    private final OrderRepository orderRepository;
    private final WebClient.Builder webClientBuilder;
    public String placeOrder(OrderRequest orderRequest){
        Order order = new Order();
        order.setOrderNumber(UUID.randomUUID().toString());
        List<OrderLineItems> orderLineItemsList = orderRequest.getOrderLineItemsDtoList().stream().map(this::mapToDto).collect(Collectors.toList());
        order.setOrderLineItemsList(orderLineItemsList);
        // Call Inventory Service in order to check whether the product is available or not
        List<String> skuCodes = order.getOrderLineItemsList()
                .stream()
                .map(OrderLineItems::getSkuCode)
                .collect(Collectors.toList());
        InventoryResponse[] inventoryResponse= webClientBuilder.build().get()
                .uri("http://inventory-service/api/inventory",
                        uriBuilder -> uriBuilder.queryParam("skuCode",skuCodes).build())
                .retrieve()
                .bodyToMono(InventoryResponse[].class)
                .block();
        boolean allProductsInStock = Arrays.stream(inventoryResponse).allMatch(InventoryResponse::isInStock);
        if(allProductsInStock){
            orderRepository.save(order);
            return "Order Placed Successfully";
        }else{
            log.info("No inventory for order ");
            throw new IllegalArgumentException("product is not in stock , please try again later");
        }
    }
    private OrderLineItems mapToDto(OrderLineItemsDto orderLineItemsDto){
        OrderLineItems orderLineItems = new OrderLineItems();
        orderLineItems.setPrice(orderLineItemsDto.getPrice());
        orderLineItems.setQuantity(orderLineItemsDto.getQuantity());
        orderLineItems.setSkuCode(orderLineItemsDto.getSkuCode());
        return orderLineItems;
    }
}
