
package com.clickstore.clickstore.dto;

import java.math.BigDecimal;

public record CartItemResponse(
        Long cartItemId,
        Long cartId,
        Long productId,
        Long variantId,
        String productName,
        String variantName,
        String sku,
        BigDecimal price,
        String currencyCode,
        Integer quantity,
        BigDecimal subtotal,
        String imageUrl
) {
}
