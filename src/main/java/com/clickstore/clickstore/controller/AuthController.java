
package com.clickstore.clickstore.controller;

import com.clickstore.clickstore.dto.LoginRequest;
import com.clickstore.clickstore.dto.LoginResponse;
import com.clickstore.clickstore.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }



    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            Authentication authentication) {

        return ResponseEntity.ok(
                Map.of(
                        "message", "Token is valid",
                        "userId", authentication.getPrincipal()
                )
        );
    }
}
