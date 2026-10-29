
package com.clickstore.clickstore.controller;

import com.clickstore.clickstore.entity.Seller;
import com.clickstore.clickstore.service.SellerService;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/sellers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSellerController {

    private final SellerService sellerService;

    public AdminSellerController(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    // View pending seller applications
    @GetMapping("/pending")
    public List<Seller> getPendingApplications() {
        return sellerService.getPendingApplications();
    }

    // View all approved sellers
    @GetMapping
    public List<Seller> getApprovedSellers() {
        return sellerService.getApprovedSellers();
    }

    // Approve a seller application
    @PutMapping("/{sellerId}/approve")
    public Seller approveSeller(
            @PathVariable Long sellerId,
            Authentication authentication) {

        Long adminUserId = getUserId(authentication);

        return sellerService.approveSeller(sellerId, adminUserId);
    }

    // Reject a seller application
    @PutMapping("/{sellerId}/reject")
    public Seller rejectSeller(
            @PathVariable Long sellerId,
            @RequestBody(required = false) Map<String, String> request,
            Authentication authentication) {

        Long adminUserId = getUserId(authentication);
        String reason = request == null ? null : request.get("reason");

        return sellerService.rejectSeller(
                sellerId,
                adminUserId,
                reason
        );
    }

    private Long getUserId(Authentication authentication) {
        try {
            return Long.parseLong(authentication.getName());
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Unable to identify logged-in admin"
            );
        }
    }
}
