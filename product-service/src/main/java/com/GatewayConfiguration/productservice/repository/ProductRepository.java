package com.GatewayConfiguration.productservice.repository;

import com.GatewayConfiguration.productservice.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductRepository extends MongoRepository<Product,String> {
}
