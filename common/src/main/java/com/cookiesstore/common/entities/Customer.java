package com.cookiesstore.common.entities;

import java.time.Instant;
import java.util.Set;
import java.util.regex.Pattern;

import org.springframework.security.crypto.bcrypt.BCrypt;

import jakarta.annotation.Nullable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity(name = "customers")
public class Customer {

    private static final Pattern BCRYPT_PATTERN = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.+");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Size(max = 128)
    @NotBlank
    private String name;

    @Column(name = "logo_url", nullable = true, length = 255)
    private String logoUrl;


    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Email
    @NotBlank
    @Size(max = 128)
    private String email;

    @Nullable
    @Size(max = 32)
    private String phone;

    @Column(nullable = false)
    private boolean active = true;


    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private Set<CustomerAddress> addresses;

    @OneToMany(mappedBy = "customer", fetch = FetchType.LAZY)
    private Set<Order> orders;

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

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public Set<CustomerAddress> getAddresses() {
        return addresses;
    }

    public Set<Order> getOrders() {
        return orders;
    }

}
