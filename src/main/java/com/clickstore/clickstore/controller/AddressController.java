
package com.clickstore.clickstore.controller;

import com.clickstore.clickstore.entity.Address;
import com.clickstore.clickstore.service.AddressService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/customer/addresses")
public class AddressController {

    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }

    // Get logged-in customer's addresses
    @GetMapping
    public List<Address> getMyAddresses(
            Authentication authentication) {

        Long userId = Long.parseLong(
                authentication.getPrincipal().toString()
        );

        return addressService.getMyAddresses(userId);
    }

    // Add a new delivery address
    @PostMapping
    public Map<String, Object> addAddress(
            Authentication authentication,
            @Valid @RequestBody AddressRequest request) {

        Long userId = Long.parseLong(
                authentication.getPrincipal().toString()
        );

        Address address = new Address();
        address.setRecipientName(request.recipientName());
        address.setPhone(request.phone());
        address.setAddressLine1(request.addressLine1());
        address.setAddressLine2(request.addressLine2());
        address.setLandmark(request.landmark());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPostalCode(request.postalCode());
        address.setCountryCode(request.countryCode());

        Address saved = addressService.addAddress(userId, address);

        return Map.of(
                "message", "Address added successfully",
                "addressId", saved.getId(),
                "recipientName", saved.getRecipientName(),
                "defaultAddress", saved.isDefaultAddress()
        );
    }

    public record AddressRequest(
            @NotBlank @Size(max = 100) String recipientName,
            @NotBlank @Size(max = 20) String phone,
            @NotBlank @Size(max = 255) String addressLine1,
            @Size(max = 255) String addressLine2,
            @Size(max = 150) String landmark,
            @NotBlank @Size(max = 100) String city,
            @NotBlank @Size(max = 100) String state,
            @NotBlank @Size(max = 20) String postalCode,
            @NotBlank @Size(min = 2, max = 2) String countryCode
    ) {}


    @PutMapping("/{addressId}")
    public Map<String, Object> updateAddress(
            Authentication authentication,
            @PathVariable Long addressId,
            @Valid @RequestBody AddressRequest request) {

        Long userId = Long.parseLong(
                authentication.getPrincipal().toString()
        );

        Address updatedAddress = new Address();
        updatedAddress.setRecipientName(request.recipientName());
        updatedAddress.setPhone(request.phone());
        updatedAddress.setAddressLine1(request.addressLine1());
        updatedAddress.setAddressLine2(request.addressLine2());
        updatedAddress.setLandmark(request.landmark());
        updatedAddress.setCity(request.city());
        updatedAddress.setState(request.state());
        updatedAddress.setPostalCode(request.postalCode());
        updatedAddress.setCountryCode(request.countryCode());

        Address saved = addressService.updateAddress(
                addressId, userId, updatedAddress
        );

        return Map.of(
                "message", "Address updated successfully",
                "addressId", saved.getId(),
                "recipientName", saved.getRecipientName(),
                "city", saved.getCity(),
                "updatedAt", saved.getUpdatedAt()
        );
    }


    @DeleteMapping("/{addressId}")
    public Map<String, String> deleteAddress(
            Authentication authentication,
            @PathVariable Long addressId) {

        Long userId = Long.parseLong(
                authentication.getPrincipal().toString()
        );

        addressService.deleteAddress(addressId, userId);

        return Map.of(
                "message", "Address deleted successfully"
        );
    }


}
