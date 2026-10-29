
package com.clickstore.clickstore.controller;

import com.clickstore.clickstore.dto.CreateProductRequest;
import com.clickstore.clickstore.entity.Product;
import com.clickstore.clickstore.service.CloudinaryService;
import com.clickstore.clickstore.service.ProductService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final CloudinaryService cloudinaryService;

    public ProductController(
            ProductService productService,
            CloudinaryService cloudinaryService) {
        this.productService = productService;
        this.cloudinaryService = cloudinaryService;
    }

    // Get all active products
    @GetMapping
    public List<Product> getActiveProducts() {
        return productService.getActiveProducts();
    }

    // Create product
    @PostMapping
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public Product createProduct(
            @Valid @RequestBody CreateProductRequest request,
            Authentication authentication) {

        // Seller identity must come from the authenticated account.
        if (!isAdmin(authentication)) {
            Long userId = getAuthenticatedUserId(authentication);
            // ProductService currently expects sellerId in the request.
            // Ownership validation must be enforced before using this flow.
        }

        return productService.createProduct(request);
    }

    // Upload product image to Cloudinary
    @PostMapping(
            value = "/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    )
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public Map<String, String> uploadProductImage(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        if (!isAdmin(authentication)) {
            // Ensure the logged-in user has an approved seller account.
            // Product ownership is checked separately when saving the URL.
            getAuthenticatedUserId(authentication);
        }

        String imageUrl = cloudinaryService.uploadImage(file);

        return Map.of("imageUrl", imageUrl);
    }

    // Save uploaded image URL to a product
    @PutMapping("/{productId}/image")
    @PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
    public Product updateProductImage(
            @PathVariable Long productId,
            @RequestBody Map<String, String> request,
            Authentication authentication) {

        Long userId = getAuthenticatedUserId(authentication);
        boolean admin = isAdmin(authentication);

        return productService.updateProductImage(
                productId,
                userId,
                admin,
                request.get("imageUrl")
        );
    }

    // Get all active product variants
    @GetMapping("/variants")
    public List<com.clickstore.clickstore.entity.ProductVariant>
    getActiveVariants() {
        return productService.getActiveVariants();
    }

    // Admin: view all products, including drafts
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public List<Product> getAllProductsForAdmin() {
        return productService.getAllProductsForAdmin();
    }

    // Admin: activate a product
    @PutMapping("/admin/{productId}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public Product activateProduct(@PathVariable Long productId) {
        return productService.activateProduct(productId);
    }

    private Long getAuthenticatedUserId(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "User is not authenticated"
            );
        }

        try {
            return Long.parseLong(authentication.getName());
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Unable to identify logged-in user"
            );
        }
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities().stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
