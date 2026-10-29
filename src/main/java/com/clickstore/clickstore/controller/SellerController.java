
package com.clickstore.clickstore.controller;

import com.clickstore.clickstore.dto.CreateSellerRequest;
import com.clickstore.clickstore.entity.Seller;
import com.clickstore.clickstore.service.SellerService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/seller")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    // Submit a seller application using the logged-in account
    @PostMapping("/apply")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Seller apply(
            @Valid @RequestBody CreateSellerRequest request,
            Authentication authentication) {

        Long userId = getUserId(authentication);

        return sellerService.applyToBecomeSeller(userId, request);
    }

    // Get the logged-in user's seller application/status
    @GetMapping("/application")
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('SELLER')")
    public Seller getMyApplication(Authentication authentication) {

        Long userId = getUserId(authentication);

        return sellerService.getMySellerApplication(userId);
    }

    private Long getUserId(Authentication authentication) {
        try {
            return Long.parseLong(authentication.getName());
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Unable to identify logged-in user"
            );
        }
    }
}
