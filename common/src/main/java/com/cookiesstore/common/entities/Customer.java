package com.cookiesstore.common.entities;

import java.time.Instant;
import java.util.regex.Pattern;

import org.springframework.security.crypto.bcrypt.BCrypt;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;

@Entity(name = "customers")
public class Customer {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.+");

    @Id
    private Long id;

    @Max(128)
    @NotBlank
    private String name;

    @Column(name = "logo_url", nullable = true, length = 255)
    private String logoUrl;


    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Email
    @NotBlank
    @Max(128)
    private String email;

    @Nullable
    @Max(32)
    private String phone;

    @Column(nullable = false)
    private boolean active = true;


    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public void setPassword(String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }

        if (BCRYPT_PATTERN.matcher(rawPassword).matches()) {
            this.passwordHash = rawPassword;
        } else {
            this.passwordHash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        }
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLogoUrl() {
        return this.logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }


    public String getPhone() {
        return this.phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return this.active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }


    
}
