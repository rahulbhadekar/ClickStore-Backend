
package com.clickstore.clickstore.service;

import com.clickstore.clickstore.dto.CartItemResponse;
import com.clickstore.clickstore.entity.Cart;
import com.clickstore.clickstore.entity.CartItem;
import com.clickstore.clickstore.entity.Product;
import com.clickstore.clickstore.entity.ProductVariant;
import com.clickstore.clickstore.repository.CartItemRepository;
import com.clickstore.clickstore.repository.ProductRepository;
import com.clickstore.clickstore.repository.ProductVariantRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartItemService {

    private final CartService cartService;
    private final CartItemRepository cartItemRepository;
    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    public CartItemService(
            CartService cartService,
            CartItemRepository cartItemRepository,
            ProductVariantRepository productVariantRepository,
            ProductRepository productRepository) {

        this.cartService = cartService;
        this.cartItemRepository = cartItemRepository;
        this.productVariantRepository = productVariantRepository;
        this.productRepository = productRepository;
    }

    // ADD ITEM TO CART
    @Transactional
    public CartItem addItem(Long userId, Long variantId, int quantity) {

        validateIds(userId, variantId);

        if (quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero"
            );
        }

        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product variant not found"
                ));

        if (!variant.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product variant is inactive"
            );
        }

        Cart cart = cartService.getOrCreateActiveCart(userId);
        OffsetDateTime now = OffsetDateTime.now();

        CartItem item = cartItemRepository
                .findByCartIdAndVariantId(cart.getId(), variantId)
                .orElseGet(() -> {
                    CartItem newItem = new CartItem();
                    newItem.setCartId(cart.getId());
                    newItem.setVariantId(variantId);
                    newItem.setQuantity(0);
                    newItem.setCreatedAt(now);
                    return newItem;
                });

        item.setQuantity(item.getQuantity() + quantity);
        item.setUpdatedAt(now);
        cart.setUpdatedAt(now);

        return cartItemRepository.save(item);
    }

    // GET CART WITH PRODUCT DETAILS
    @Transactional(readOnly = true)
    public List<CartItemResponse> getItems(Long userId) {

        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid user ID is required"
            );
        }

        List<CartItemResponse> response = new ArrayList<>();

        cartService.findActiveCart(userId).ifPresent(cart -> {

            List<CartItem> items =
                    cartItemRepository.findByCartId(cart.getId());

            for (CartItem item : items) {

                ProductVariant variant =
                        productVariantRepository.findById(item.getVariantId())
                                .orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Product variant not found"
                                ));

                Product product =
                        productRepository.findById(variant.getProductId())
                                .orElseThrow(() -> new ResponseStatusException(
                                        HttpStatus.NOT_FOUND,
                                        "Product not found"
                                ));

                BigDecimal subtotal = variant.getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));

                response.add(new CartItemResponse(
                        item.getId(),
                        item.getCartId(),
                        product.getId(),
                        variant.getId(),
                        product.getName(),
                        variant.getVariantName(),
                        variant.getSku(),
                        variant.getPrice(),
                        variant.getCurrencyCode(),
                        item.getQuantity(),
                        subtotal,
                        product.getImageUrl()
                ));
            }
        });

        return response;
    }

    // UPDATE ITEM QUANTITY
    @Transactional
    public CartItem updateQuantity(
            Long userId,
            Long itemId,
            int quantity) {

        if (userId == null || userId <= 0 ||
                itemId == null || itemId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid user and cart item IDs are required"
            );
        }

        if (quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Quantity must be greater than zero"
            );
        }

        Cart cart = cartService.findActiveCart(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Active cart not found"
                ));

        CartItem item = cartItemRepository.findById(itemId)
                .filter(existing ->
                        existing.getCartId().equals(cart.getId()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found"
                ));

        item.setQuantity(quantity);
        item.setUpdatedAt(OffsetDateTime.now());
        cart.setUpdatedAt(OffsetDateTime.now());

        return cartItemRepository.save(item);
    }

    // REMOVE ITEM FROM CART
    @Transactional
    public void removeItem(Long userId, Long itemId) {

        if (userId == null || userId <= 0 ||
                itemId == null || itemId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid user and cart item IDs are required"
            );
        }

        Cart cart = cartService.findActiveCart(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Active cart not found"
                ));

        CartItem item = cartItemRepository.findById(itemId)
                .filter(existing ->
                        existing.getCartId().equals(cart.getId()))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found"
                ));

        cartItemRepository.delete(item);
        cart.setUpdatedAt(OffsetDateTime.now());
    }

    private void validateIds(Long userId, Long variantId) {

        if (userId == null || userId <= 0 ||
                variantId == null || variantId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid user and variant IDs are required"
            );
        }
    }
}
