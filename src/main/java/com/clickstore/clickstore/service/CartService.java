
package com.clickstore.clickstore.service;

import com.clickstore.clickstore.entity.Cart;
import com.clickstore.clickstore.repository.CartRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Optional;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    @Transactional
    public Cart getOrCreateActiveCart(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Valid user ID is required");
        }

        return findActiveCart(userId).orElseGet(() -> {
            OffsetDateTime now = OffsetDateTime.now();

            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setStatus("ACTIVE");
            cart.setCreatedAt(now);
            cart.setUpdatedAt(now);

            return cartRepository.save(cart);
        });
    }

    @Transactional(readOnly = true)
    public Optional<Cart> findActiveCart(Long userId) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Valid user ID is required");
        }

        return cartRepository.findByUserIdAndStatus(userId, "ACTIVE");
    }
}
