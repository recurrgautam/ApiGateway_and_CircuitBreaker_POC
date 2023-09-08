package com.GatewayConfiguration.orderservice.repository;

import com.GatewayConfiguration.orderservice.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order,Long> {
}
