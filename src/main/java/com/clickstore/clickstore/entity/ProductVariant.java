
package com.clickstore.clickstore.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "product_variants")
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(name = "variant_name", nullable = false, length = 120)
    private String variantName;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "currency_code", nullable = false, length = 3)
    private String currencyCode;

    @Column(name = "is_active", nullable = false)
    private boolean active;

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getSku() { return sku; }
    public String getVariantName() { return variantName; }
    public BigDecimal getPrice() { return price; }
    public String getCurrencyCode() { return currencyCode; }
    public boolean isActive() { return active; }

    public void setProductId(Long productId) { this.productId = productId; }
    public void setSku(String sku) { this.sku = sku; }
    public void setVariantName(String variantName) { this.variantName = variantName; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public void setCurrencyCode(String currencyCode) { this.currencyCode = currencyCode; }
    public void setActive(boolean active) { this.active = active; }
}
