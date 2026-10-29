
package com.clickstore.clickstore.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "inventory")
public class Inventory {

    @Id
    @Column(name = "variant_id")
    private Long variantId;

    @Column(name = "quantity_on_hand", nullable = false)
    private int quantityOnHand;

    @Column(name = "quantity_reserved", nullable = false)
    private int quantityReserved;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Inventory() {
    }

    public Inventory(Long variantId, int quantityOnHand) {
        this.variantId = variantId;
        this.quantityOnHand = quantityOnHand;
        this.quantityReserved = 0;
        this.updatedAt = OffsetDateTime.now();
    }

    public Long getVariantId() {
        return variantId;
    }

    public int getQuantityOnHand() {
        return quantityOnHand;
    }

    public int getQuantityReserved() {
        return quantityReserved;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
