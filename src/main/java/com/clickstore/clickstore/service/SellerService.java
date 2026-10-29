
package com.clickstore.clickstore.service;

import com.clickstore.clickstore.dto.CreateSellerRequest;
import com.clickstore.clickstore.entity.Seller;
import com.clickstore.clickstore.repository.SellerRepository;
import com.clickstore.clickstore.repository.UserRoleRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.text.Normalizer;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class SellerService {

    private final SellerRepository sellerRepository;
    private final UserRoleRepository userRoleRepository;

    public SellerService(
            SellerRepository sellerRepository,
            UserRoleRepository userRoleRepository) {

        this.sellerRepository = sellerRepository;
        this.userRoleRepository = userRoleRepository;
    }

    // Customer applies to become a seller
    @Transactional
    public Seller applyToBecomeSeller(
            Long userId,
            CreateSellerRequest request) {

        if (request.getStoreName() == null
                || request.getStoreName().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Store name is required"
            );
        }

        String storeName = request.getStoreName().trim();

        if (storeName.length() > 150) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Store name must be at most 150 characters"
            );
        }

        if (sellerRepository.findByUserId(userId).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "A seller application already exists for this account"
            );
        }

        String baseSlug = createSlug(storeName);

        if (baseSlug.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Please choose a valid store name"
            );
        }

        String storeSlug = baseSlug;
        int suffix = 2;

        while (sellerRepository.existsByStoreSlug(storeSlug)) {
            storeSlug = baseSlug + "-" + suffix++;
        }

        OffsetDateTime now = OffsetDateTime.now();

        Seller seller = new Seller();
        seller.setUserId(userId);
        seller.setStoreName(storeName);
        seller.setStoreSlug(storeSlug);
        seller.setStatus("PENDING");
        seller.setCreatedAt(now);
        seller.setUpdatedAt(now);

        return sellerRepository.save(seller);
    }

    // Get current user's seller application
    public Seller getMySellerApplication(Long userId) {
        return sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No seller application found"
                ));
    }

    // Admin: view pending seller applications
    public List<Seller> getPendingApplications() {
        return sellerRepository.findByStatus("PENDING");
    }

    // Admin: approve a pending seller application
    @Transactional
    public Seller approveSeller(Long sellerId, Long adminUserId) {

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Seller application not found"
                ));

        if ("APPROVED".equals(seller.getStatus())) {
            return seller;
        }

        if (!"PENDING".equals(seller.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending applications can be approved"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        seller.setStatus("APPROVED");
        seller.setReviewedBy(adminUserId);
        seller.setReviewedAt(now);
        seller.setRejectionReason(null);
        seller.setUpdatedAt(now);

        Seller savedSeller = sellerRepository.save(seller);

        // Assign SELLER role after approval
        userRoleRepository.assignSellerRole(seller.getUserId());

        return savedSeller;
    }

    // Admin: reject a pending seller application
    @Transactional
    public Seller rejectSeller(
            Long sellerId,
            Long adminUserId,
            String reason) {

        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Seller application not found"
                ));

        if (!"PENDING".equals(seller.getStatus())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Only pending applications can be rejected"
            );
        }

        OffsetDateTime now = OffsetDateTime.now();

        seller.setStatus("REJECTED");
        seller.setReviewedBy(adminUserId);
        seller.setReviewedAt(now);
        seller.setRejectionReason(
                reason == null || reason.isBlank()
                        ? "Application rejected by admin"
                        : reason.trim()
        );
        seller.setUpdatedAt(now);

        return sellerRepository.save(seller);
    }

    // Generate a URL-friendly store slug
    private String createSlug(String storeName) {

        String normalized = Normalizer.normalize(
                storeName.toLowerCase(Locale.ROOT),
                Normalizer.Form.NFD
        );

        return normalized
                .replaceAll("\\p{M}", "")
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
    }



    // Admin: get all approved sellers
    @Transactional(readOnly = true)
    public List<Seller> getApprovedSellers() {
        return sellerRepository.findByStatus("APPROVED");
    }

}
