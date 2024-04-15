package com.codewitharjun.fullstackbackend.repository;

import com.codewitharjun.fullstackbackend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
    // You can define additional query methods here if needed
}
