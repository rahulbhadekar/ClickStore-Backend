
package com.clickstore.clickstore.repository;

import com.clickstore.clickstore.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ProductRepository extends JpaRepository<Product, Long> {

    List<Product> findByStatus(String status);

    List<Product> findBySellerId(Long sellerId);

    List<Product> findAllByOrderByIdDesc();
}
