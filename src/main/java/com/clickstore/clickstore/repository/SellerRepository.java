
package com.clickstore.clickstore.repository;

import com.clickstore.clickstore.entity.Seller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SellerRepository extends JpaRepository<Seller, Long> {

    Optional<Seller> findByIdAndStatus(Long id, String status);

    Optional<Seller> findByUserId(Long userId);

    List<Seller> findByStatus(String status);

    boolean existsByStoreSlug(String storeSlug);
}
