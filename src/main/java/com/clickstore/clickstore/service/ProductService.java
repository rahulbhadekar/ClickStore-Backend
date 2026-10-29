
package com.clickstore.clickstore.service;

import com.clickstore.clickstore.dto.CreateProductRequest;
import com.clickstore.clickstore.entity.Inventory;
import com.clickstore.clickstore.entity.Product;
import com.clickstore.clickstore.entity.ProductVariant;
import com.clickstore.clickstore.entity.Seller;
import com.clickstore.clickstore.repository.CategoryRepository;
import com.clickstore.clickstore.repository.InventoryRepository;
import com.clickstore.clickstore.repository.ProductRepository;
import com.clickstore.clickstore.repository.ProductVariantRepository;
import com.clickstore.clickstore.repository.SellerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    private final InventoryRepository inventoryRepository;
    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(
            ProductRepository productRepository,
            ProductVariantRepository variantRepository,
            InventoryRepository inventoryRepository,
            SellerRepository sellerRepository,
            CategoryRepository categoryRepository) {

        this.productRepository = productRepository;
        this.variantRepository = variantRepository;
        this.inventoryRepository = inventoryRepository;
        this.sellerRepository = sellerRepository;
        this.categoryRepository = categoryRepository;
    }

    // Fetch all active products for the public storefront
    public List<Product> getActiveProducts() {
        return productRepository.findByStatus("ACTIVE");
    }

    // Fetch active product variants with their prices
    public List<ProductVariant> getActiveVariants() {
        return variantRepository.findByActiveTrue();
    }

    // Admin: fetch all products, including DRAFT products
    public List<Product> getAllProductsForAdmin() {
        return productRepository.findAllByOrderByIdDesc();
    }

    // Admin: activate a product
    @Transactional
    public Product activateProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));

        if ("ACTIVE".equalsIgnoreCase(product.getStatus())) {
            return product;
        }

        // Verify the product has an active category
        categoryRepository
                .findByIdAndActiveTrue(product.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cannot activate product: category is missing or inactive"
                ));

        // Verify the product belongs to an approved seller
        sellerRepository
                .findByIdAndStatus(product.getSellerId(), "APPROVED")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Cannot activate product: seller is not approved"
                ));

        product.setStatus("ACTIVE");
        product.setUpdatedAt(OffsetDateTime.now());

        return productRepository.save(product);
    }

    // Create product, variant and inventory in one transaction
    @Transactional
    public Product createProduct(CreateProductRequest request) {

        if (request.getSellerId() == null
                || request.getCategoryId() == null
                || request.getName() == null
                || request.getName().isBlank()
                || request.getSlug() == null
                || request.getSlug().isBlank()
                || request.getSku() == null
                || request.getSku().isBlank()
                || request.getPrice() == null
                || request.getInitialStock() == null) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Required product fields are missing"
            );
        }

        if (request.getPrice().signum() <= 0
                || request.getInitialStock() < 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Price must be positive and stock cannot be negative"
            );
        }

        // Only approved sellers can create products
        sellerRepository
                .findByIdAndStatus(request.getSellerId(), "APPROVED")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Seller is not approved"
                ));

        // Category must exist and be active
        categoryRepository
                .findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Category does not exist or is inactive"
                ));

        OffsetDateTime now = OffsetDateTime.now();

        Product product = new Product();
        product.setSellerId(request.getSellerId());
        product.setCategoryId(request.getCategoryId());
        product.setName(request.getName().trim());
        product.setSlug(request.getSlug().trim());
        product.setDescription(request.getDescription());
        product.setStatus("DRAFT");
        product.setCreatedAt(now);
        product.setUpdatedAt(now);
        product.setImageUrl(request.getImageUrl());

        Product savedProduct = productRepository.save(product);

        ProductVariant variant = new ProductVariant();
        variant.setProductId(savedProduct.getId());
        variant.setSku(request.getSku().trim());
        variant.setVariantName(
                request.getVariantName() == null
                        || request.getVariantName().isBlank()
                        ? "Default"
                        : request.getVariantName().trim()
        );
        variant.setPrice(request.getPrice());
        variant.setCurrencyCode("INR");
        variant.setActive(true);

        ProductVariant savedVariant = variantRepository.save(variant);

        Inventory inventory = new Inventory(
                savedVariant.getId(),
                request.getInitialStock()
        );

        inventoryRepository.save(inventory);

        return savedProduct;
    }

    // Update product image securely
    @Transactional
    public Product updateProductImage(
            Long productId,
            Long userId,
            boolean isAdmin,
            String imageUrl) {

        if (imageUrl == null
                || imageUrl.isBlank()
                || !imageUrl.startsWith("https://")) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A valid HTTPS image URL is required"
            );
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"
                ));

        if (!isAdmin) {
            Seller seller = sellerRepository.findByUserId(userId)
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Seller account not found"
                    ));

            if (!seller.getId().equals(product.getSellerId())) {
                throw new AccessDeniedException(
                        "You can update images only for your own products"
                );
            }
        }

        product.setImageUrl(imageUrl);
        product.setUpdatedAt(OffsetDateTime.now());

        return productRepository.save(product);
    }
}
