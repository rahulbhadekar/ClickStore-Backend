
package com.clickstore.clickstore.controller;

import com.clickstore.clickstore.dto.PlaceOrderRequest;
import com.clickstore.clickstore.entity.Order;
import com.clickstore.clickstore.service.OrderService;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/api/customer/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping
    public List<Order> getMyOrders(Authentication authentication) {
        Long customerId = getCustomerId(authentication);
        return orderService.getMyOrders(customerId);
    }

    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Order> getAllOrdersForAdmin() {
        return orderService.getAllOrdersForAdmin();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Order placeOrder(
            @RequestBody PlaceOrderRequest request,
            Authentication authentication
    ) {
        Long customerId = getCustomerId(authentication);

        if (request == null || request.addressId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "addressId is required"
            );
        }

        if (request.cartItemIds() == null
                || request.cartItemIds().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please select at least one cart item"
            );
        }

        return orderService.placeOrder(
                customerId,
                request.addressId(),
                request.cartItemIds()
        );
    }

    private Long getCustomerId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in first"
            );
        }

        try {
            return Long.parseLong(authentication.getName());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid authentication"
            );
        }
    }
}
