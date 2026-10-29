
package com.clickstore.clickstore.service;

import com.clickstore.clickstore.entity.Address;
import com.clickstore.clickstore.repository.AddressRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;

    public AddressService(AddressRepository addressRepository) {
        this.addressRepository = addressRepository;
    }

    public List<Address> getMyAddresses(Long userId) {
        return addressRepository
                .findByUserIdOrderByDefaultAddressDescCreatedAtDesc(userId);
    }

    @Transactional
    public Address addAddress(Long userId, Address address) {

        // ID client se accept nahi karni
        address.setUserId(userId);

        OffsetDateTime now = OffsetDateTime.now();
        address.setCreatedAt(now);
        address.setUpdatedAt(now);

        // First saved address becomes the default address
        if (!addressRepository.existsByUserId(userId)) {
            address.setDefaultAddress(true);
        } else {
            address.setDefaultAddress(false);
        }

        return addressRepository.save(address);
    }

    public Address getMyAddressById(Long addressId, Long userId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Address not found"
                ));
    }


    @Transactional
    public Address updateAddress(
            Long addressId,
            Long userId,
            Address updatedAddress) {

        Address existing = addressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Address not found"
                ));

        existing.setRecipientName(updatedAddress.getRecipientName());
        existing.setPhone(updatedAddress.getPhone());
        existing.setAddressLine1(updatedAddress.getAddressLine1());
        existing.setAddressLine2(updatedAddress.getAddressLine2());
        existing.setLandmark(updatedAddress.getLandmark());
        existing.setCity(updatedAddress.getCity());
        existing.setState(updatedAddress.getState());
        existing.setPostalCode(updatedAddress.getPostalCode());
        existing.setCountryCode(updatedAddress.getCountryCode());
        existing.setUpdatedAt(OffsetDateTime.now());

        return addressRepository.save(existing);
    }


    @Transactional
    public void deleteAddress(Long addressId, Long userId) {

        Address address = addressRepository
                .findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Address not found"
                ));

        addressRepository.delete(address);
    }


}
