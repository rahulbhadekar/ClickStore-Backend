
package com.clickstore.clickstore.dto;

import java.util.List;

public record PlaceOrderRequest(
        Long addressId,
        List<Long> cartItemIds
) {
}
