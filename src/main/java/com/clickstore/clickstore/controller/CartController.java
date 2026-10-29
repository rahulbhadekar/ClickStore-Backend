
package com.clickstore.clickstore.controller;

import com.clickstore.clickstore.dto.CartItemResponse;
import com.clickstore.clickstore.entity.Cart;
import com.clickstore.clickstore.service.CartItemService;
import com.clickstore.clickstore.service.CartService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/customer/cart")
public class CartController {

    private final CartService cartService;
    private final CartItemService cartItemService;

    public CartController(
            CartService cartService,
            CartItemService cartItemService) {
        this.cartService = cartService;
        this.cartItemService = cartItemService;
    }

    // GET CART ITEMS WITH PRODUCT DETAILS
    @GetMapping
    public List<CartItemResponse> getMyCart(
            Authentication authentication) {

        Long userId = getUserId(authentication);
        return cartItemService.getItems(userId);
    }

    // GET OR CREATE ACTIVE CART
    @GetMapping("/details")
    public Cart getCartDetails(Authentication authentication) {
        Long userId = getUserId(authentication);
        return cartService.getOrCreateActiveCart(userId);
    }

    private Long getUserId(Authentication authentication) {
        if (authentication == null ||
                authentication.getPrincipal() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required");
        }

        try {
            return Long.parseLong(
                    authentication.getPrincipal().toString());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid authenticated user");
        }
    }
}
