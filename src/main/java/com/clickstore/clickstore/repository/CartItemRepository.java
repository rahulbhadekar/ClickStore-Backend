
package com.clickstore.clickstore.repository;

import com.clickstore.clickstore.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Get all items from a cart
    List<CartItem> findByCartId(Long cartId);

    // Find a cart item by cart ID and variant ID
    Optional<CartItem> findByCartIdAndVariantId(
            Long cartId,
            Long variantId
    );

    // Find selected cart items by cart ID and item IDs
    List<CartItem> findByCartIdAndIdIn(
            Long cartId,
            Collection<Long> ids
    );

    // Delete a cart item by cart ID and variant ID
    void deleteByCartIdAndVariantId(
            Long cartId,
            Long variantId
    );
}
