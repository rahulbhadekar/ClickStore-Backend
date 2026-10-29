
package com.clickstore.clickstore.dto;

import java.util.List;

public class LoginResponse {

    private final String message;
    private final String token;
    private final String tokenType;
    private final Long userId;
    private final String fullName;
    private final String email;
    private final List<String> roles;

    public LoginResponse(
            String message,
            String token,
            Long userId,
            String fullName,
            String email,
            List<String> roles) {

        this.message = message;
        this.token = token;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.fullName = fullName;
        this.email = email;
        this.roles = roles;
    }

    public String getMessage() {
        return message;
    }

    public String getToken() {
        return token;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Long getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public List<String> getRoles() {
        return roles;
    }
}
