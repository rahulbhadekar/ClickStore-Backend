
package com.clickstore.clickstore.dto;

public class ProfileResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phone;

    public ProfileResponse() {
    }

    public ProfileResponse(
            Long id,
            String fullName,
            String email,
            String phone
    ) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }

    public Long getId() {
        return id;
    }

    public String getFullName() {
        return fullName;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }
}
