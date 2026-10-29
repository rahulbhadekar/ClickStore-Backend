
package com.clickstore.clickstore.entity;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_contacts")
public class UserContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "contact_type", nullable = false)
    private String contactType;

    @Column(name = "contact_value", nullable = false)
    private String contactValue;

    @Column(name = "is_verified", nullable = false)
    private boolean verified;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

    @Column(name = "verified_at")
    private OffsetDateTime verifiedAt;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    public UserContact() {
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    public String getContactType() {
        return contactType;
    }

    public String getContactValue() {
        return contactValue;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public void setContactValue(String contactValue) {
        this.contactValue = contactValue;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(OffsetDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}
