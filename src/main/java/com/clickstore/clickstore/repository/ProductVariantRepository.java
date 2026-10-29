package com.clickstore.clickstore.repository;

import com.clickstore.clickstore.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductVariantRepository
        extends JpaRepository<ProductVariant, Long> {

    List<ProductVariant> findByActiveTrue();

    List<ProductVariant> findByProductIdAndActiveTrue(Long productId);
}