
package com.clickstore.clickstore.controller;

import com.clickstore.clickstore.dto.UpdateProfileRequest;
import com.clickstore.clickstore.entity.User;
import com.clickstore.clickstore.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    private final UserRepository userRepository;

    public CustomerController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/test")
    public Map<String, String> customerTest(
            Authentication authentication) {

        return Map.of(
                "message", "Customer access successful",
                "userId", authentication.getPrincipal().toString()
        );
    }

    @GetMapping("/profile")
    public Map<String, Object> getProfile(
            Authentication authentication) {

        User user = getLoggedInUser(authentication);

        return Map.of(
                "userId", user.getId(),
                "fullName", user.getFullName(),
                "accountStatus", user.getAccountStatus(),
                "createdAt", user.getCreatedAt()
        );
    }

    @PutMapping("/profile")
    public Map<String, Object> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        User user = getLoggedInUser(authentication);

        user.setFullName(request.getFullName().trim());
        user.setUpdatedAt(java.time.OffsetDateTime.now());

        User updatedUser = userRepository.save(user);

        return Map.of(
                "message", "Profile updated successfully",
                "userId", updatedUser.getId(),
                "fullName", updatedUser.getFullName()
        );
    }

    private User getLoggedInUser(Authentication authentication) {

        Long userId = Long.parseLong(
                authentication.getPrincipal().toString()
        );

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));
    }
}
