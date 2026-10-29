
package com.clickstore.clickstore.controller;

import com.clickstore.clickstore.entity.CartItem;
import com.clickstore.clickstore.service.CartItemService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import com.clickstore.clickstore.dto.CartItemResponse;
import java.util.List;

@RestController
@RequestMapping("/api/customer/cart/items")
public class CartItemController {

    private final CartItemService cartItemService;

    public CartItemController(CartItemService cartItemService) {
        this.cartItemService = cartItemService;
    }

    @GetMapping
    public List<CartItemResponse> getCartItems(
            Authentication authentication) {

        Long userId = getUserId(authentication);
        return cartItemService.getItems(userId);
    }


    @PostMapping
    public Map<String, Object> addItem(
            Authentication authentication,
            @Valid @RequestBody AddCartItemRequest request) {

        Long userId = getUserId(authentication);

        CartItem item = cartItemService.addItem(
                userId, request.variantId(), request.quantity());

        return Map.of(
                "message", "Item added to cart successfully",
                "cartItemId", item.getId(),
                "variantId", item.getVariantId(),
                "quantity", item.getQuantity()
        );
    }

    @PutMapping("/{itemId}")
    public Map<String, Object> updateQuantity(
            Authentication authentication,
            @PathVariable Long itemId,
            @Valid @RequestBody UpdateCartItemRequest request) {

        Long userId = getUserId(authentication);

        CartItem item = cartItemService.updateQuantity(
                userId, itemId, request.quantity());

        return Map.of(
                "message", "Cart item quantity updated successfully",
                "cartItemId", item.getId(),
                "variantId", item.getVariantId(),
                "quantity", item.getQuantity()
        );
    }

    @DeleteMapping("/{itemId}")
    @ResponseStatus(HttpStatus.OK)
    public Map<String, String> removeItem(
            Authentication authentication,
            @PathVariable Long itemId) {

        Long userId = getUserId(authentication);
        cartItemService.removeItem(userId, itemId);

        return Map.of("message", "Cart item removed successfully");
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        try {
            return Long.parseLong(authentication.getPrincipal().toString());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid authenticated user");
        }
    }

    public record AddCartItemRequest(
            @NotNull @Positive Long variantId,
            @NotNull @Positive Integer quantity) {
    }

    public record UpdateCartItemRequest(
            @NotNull @Positive Integer quantity) {
    }
}
