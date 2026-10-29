
package com.clickstore.clickstore.repository;

import com.clickstore.clickstore.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findBySellerOrderId(Long sellerOrderId);

    List<OrderItem> findBySellerId(Long sellerId);

    List<OrderItem> findBySellerOrderIdIn(List<Long> sellerOrderIds);
}
