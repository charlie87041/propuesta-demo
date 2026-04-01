package com.cookiesstore.common.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "product_types")
public class ProductType {

    @Id
    @Column(nullable = false, length = 30)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "supports_components", nullable = false)
    private boolean supportsComponents;

    @Column(name = "supports_variants", nullable = false)
    private boolean supportsVariants;

    @Column(name = "supports_addons", nullable = false)
    private boolean supportsAddons;

    @Column(name = "default_is_listable", nullable = false)
    private boolean defaultIsListable;

    @Column(name = "default_is_searchable", nullable = false)
    private boolean defaultIsSearchable;

    @Column(name = "default_is_purchasable_alone", nullable = false)
    private boolean defaultIsPurchasableAlone;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @OneToMany(mappedBy = "productType")
    private Set<Product> products;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        if (this.createdAt == null) {
            this.createdAt = now;
        }
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isSupportsComponents() {
        return supportsComponents;
    }

    public void setSupportsComponents(boolean supportsComponents) {
        this.supportsComponents = supportsComponents;
    }

    public boolean isSupportsVariants() {
        return supportsVariants;
    }

    public void setSupportsVariants(boolean supportsVariants) {
        this.supportsVariants = supportsVariants;
    }

    public boolean isSupportsAddons() {
        return supportsAddons;
    }

    public void setSupportsAddons(boolean supportsAddons) {
        this.supportsAddons = supportsAddons;
    }

    public boolean isDefaultIsListable() {
        return defaultIsListable;
    }

    public void setDefaultIsListable(boolean defaultIsListable) {
        this.defaultIsListable = defaultIsListable;
    }

    public boolean isDefaultIsSearchable() {
        return defaultIsSearchable;
    }

    public void setDefaultIsSearchable(boolean defaultIsSearchable) {
        this.defaultIsSearchable = defaultIsSearchable;
    }

    public boolean isDefaultIsPurchasableAlone() {
        return defaultIsPurchasableAlone;
    }

    public void setDefaultIsPurchasableAlone(boolean defaultIsPurchasableAlone) {
        this.defaultIsPurchasableAlone = defaultIsPurchasableAlone;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Set<Product> getProducts() {
        return products;
    }
}
