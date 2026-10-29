
package com.clickstore.clickstore.repository;

import com.clickstore.clickstore.entity.SellerOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellerOrderRepository
        extends JpaRepository<SellerOrder, Long> {

    List<SellerOrder> findByOrderId(Long orderId);

    List<SellerOrder> findBySellerId(Long sellerId);

    List<SellerOrder> findByOrderIdOrderByCreatedAtDesc(Long orderId);
}
