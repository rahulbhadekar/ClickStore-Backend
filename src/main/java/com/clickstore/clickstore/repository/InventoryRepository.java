
package com.clickstore.clickstore.repository;

import com.clickstore.clickstore.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryRepository
        extends JpaRepository<Inventory, Long> {
}
