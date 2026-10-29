
package com.clickstore.clickstore.controller;

import com.clickstore.clickstore.dto.CreateProductRequest;
import com.clickstore.clickstore.entity.Product;
import com.clickstore.clickstore.entity.Seller;
import com.clickstore.clickstore.repository.ProductRepository;
import com.clickstore.clickstore.repository.SellerRepository;
import com.clickstore.clickstore.service.ProductService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/seller/products")
@PreAuthorize("hasRole('SELLER')")
public class SellerProductController {

    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final ProductService productService;

    public SellerProductController(
            ProductRepository productRepository,
            SellerRepository sellerRepository,
            ProductService productService) {
        this.productRepository = productRepository;
        this.sellerRepository = sellerRepository;
        this.productService = productService;
    }

    private Seller getApprovedSeller(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Please login first");
        }

        Long userId;
        try {
            userId = Long.parseLong(authentication.getName());
        } catch (NumberFormatException ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid user identity");
        }

        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN, "Seller account not found"));

        if (!"APPROVED".equalsIgnoreCase(seller.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN, "Seller account is not approved");
        }

        return seller;
    }

    @GetMapping("/my-products")
    public List<Product> getMyProducts(Authentication authentication) {
        Seller seller = getApprovedSeller(authentication);
        return productRepository.findBySellerId(seller.getId());
    }

    @PostMapping
    public Product createProduct(
            @Valid @RequestBody CreateProductRequest request,
            Authentication authentication) {

        Seller seller = getApprovedSeller(authentication);

        // Seller ID comes from the authenticated account,
        // not from the frontend request.
        request.setSellerId(seller.getId());

        return productService.createProduct(request);
    }
}
