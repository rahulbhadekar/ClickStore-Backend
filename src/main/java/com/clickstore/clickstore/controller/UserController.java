
package com.clickstore.clickstore.controller;

import com.clickstore.clickstore.dto.RegisterRequest;
import com.clickstore.clickstore.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import com.clickstore.clickstore.dto.ProfileResponse;
import org.springframework.security.core.Authentication;import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import com.clickstore.clickstore.dto.ProfileUpdateRequest;
import org.springframework.web.bind.annotation.PutMapping;


@RestController
@RequestMapping("/api/auth")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(
            @Valid @RequestBody RegisterRequest request) {

        Long userId = userService.register(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "message", "Registration successful",
                        "userId", userId
                ));
    }


    @GetMapping("/profile")
    public ResponseEntity<ProfileResponse> getProfile(
            Authentication authentication
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        Object principal = authentication.getPrincipal();

        Long userId;
        try {
            userId = Long.valueOf(principal.toString());
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid authenticated user"
            );
        }

        return ResponseEntity.ok(userService.getProfile(userId));
    }


    @PutMapping("/profile")
    public ResponseEntity<ProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody ProfileUpdateRequest request
    ) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Authentication required"
            );
        }

        Long userId;

        try {
            userId = Long.valueOf(
                    authentication.getPrincipal().toString()
            );
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid authenticated user"
            );
        }

        ProfileResponse updatedProfile =
                userService.updateProfile(userId, request);

        return ResponseEntity.ok(updatedProfile);
    }





}
