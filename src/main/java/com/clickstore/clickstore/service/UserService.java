
package com.clickstore.clickstore.service;

import com.clickstore.clickstore.dto.RegisterRequest;
import com.clickstore.clickstore.entity.User;
import com.clickstore.clickstore.entity.UserContact;
import com.clickstore.clickstore.repository.UserContactRepository;
import com.clickstore.clickstore.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.clickstore.clickstore.repository.UserRoleRepository;
import java.time.OffsetDateTime;
import java.util.Locale;
import com.clickstore.clickstore.dto.ProfileResponse;
import java.util.List;
import com.clickstore.clickstore.dto.ProfileUpdateRequest;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserContactRepository contactRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleRepository userRoleRepository;

    public UserService(
            UserRepository userRepository,
            UserContactRepository contactRepository,
            PasswordEncoder passwordEncoder,
            UserRoleRepository userRoleRepository) {

        this.userRepository = userRepository;
        this.contactRepository = contactRepository;
        this.passwordEncoder = passwordEncoder;
        this.userRoleRepository = userRoleRepository;
    }


    @Transactional
    public Long register(RegisterRequest request) {

        System.out.println("REGISTER SERVICE STARTED");

        // Step 1: Email ko normalize karo
        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        System.out.println("REGISTER: checking email");

        // Step 2: Duplicate email check karo
        if (contactRepository
                .findByContactTypeAndContactValue("EMAIL", email)
                .isPresent()) {

            System.out.println("REGISTER: email already exists");

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Email is already registered"
            );
        }

        System.out.println("REGISTER: email is available");

        // Step 3: User details set karo
        OffsetDateTime now = OffsetDateTime.now();

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );
        user.setAccountStatus("ACTIVE");
        user.setCreatedAt(now);
        user.setUpdatedAt(now);

        // Step 4: User save karo
        User savedUser = userRepository.save(user);

        System.out.println("REGISTER: user saved, ID = " + savedUser.getId());

        // Step 5: Customer role assign karo
        userRoleRepository.assignCustomerRole(savedUser.getId());

        // Step 6: Email contact save karo
        UserContact contact = new UserContact();
        contact.setUserId(savedUser.getId());
        contact.setContactType("EMAIL");
        contact.setContactValue(email);
        contact.setVerified(false);
        contact.setPrimary(true);
        contact.setCreatedAt(now);
        contact.setUpdatedAt(now);

        contactRepository.save(contact);

        System.out.println("REGISTER: email contact saved");

        // Step 7: User ID return karo
        return savedUser.getId();
    }


    @Transactional(readOnly = true)
    public ProfileResponse getProfile(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        List<UserContact> contacts =
                contactRepository.findByUserId(userId);

        String email = contacts.stream()
                .filter(contact ->
                        "EMAIL".equalsIgnoreCase(contact.getContactType()))
                .map(UserContact::getContactValue)
                .findFirst()
                .orElse(null);

        String phone = contacts.stream()
                .filter(contact ->
                        "PHONE".equalsIgnoreCase(contact.getContactType()))
                .map(UserContact::getContactValue)
                .findFirst()
                .orElse(null);

        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                email,
                phone
        );
    }



    @Transactional
    public ProfileResponse updateProfile(
            Long userId,
            ProfileUpdateRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        String fullName = request.getFullName().trim();

        if (fullName.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Full name is required"
            );
        }

        user.setFullName(fullName);
        user.setUpdatedAt(OffsetDateTime.now());

        userRepository.save(user);

        String phone = request.getPhone() == null
                ? ""
                : request.getPhone().trim();

        UserContact phoneContact = contactRepository
                .findByUserIdAndContactType(userId, "PHONE")
                .orElse(null);

        if (phone.isEmpty()) {
            if (phoneContact != null) {
                contactRepository.delete(phoneContact);
            }
        } else {
            if (phoneContact == null) {
                phoneContact = new UserContact();
                phoneContact.setUserId(userId);
                phoneContact.setContactType("PHONE");
                phoneContact.setVerified(false);
                phoneContact.setPrimary(false);
                phoneContact.setCreatedAt(OffsetDateTime.now());
            }

            phoneContact.setContactValue(phone);
            phoneContact.setUpdatedAt(OffsetDateTime.now());

            contactRepository.save(phoneContact);
        }

        return getProfile(userId);
    }




}