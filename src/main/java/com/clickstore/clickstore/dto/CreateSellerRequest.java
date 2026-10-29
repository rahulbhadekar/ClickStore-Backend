
package com.clickstore.clickstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CreateSellerRequest {

    @NotBlank(message = "Store name is required")
    @Size(max = 150, message = "Store name must be at most 150 characters")
    private String storeName;

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
