package com.cookiesstore.common.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.List;
import java.util.Set;

@Entity
@Table(
    name = "products",
    indexes = {
        @Index(name = "idx_products_sku", columnList = "sku", unique = true),
        @Index(name = "idx_products_slug", columnList = "slug", unique = true),
        @Index(name = "idx_products_category", columnList = "category_id"),
        @Index(name = "idx_products_active_visible", columnList = "active,visible"),
        @Index(name = "idx_products_name", columnList = "name")
    }
)
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String sku;

    @Column(nullable = false, length = 180)
    private String name;

    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_price_id")
    private Price currentPrice;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "template_id")
    private ProductTemplate template;

    @Column(name = "product_type_code", nullable = false, length = 30)
    private String productTypeCode = "SIMPLE";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_type_code", insertable = false, updatable = false)
    private ProductType productType;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false)
    private boolean visible = true;

    @Column(name = "is_listable")
    private Boolean listable;

    @Column(name = "is_searchable")
    private Boolean searchable;

    @Column(name = "is_purchasable_alone")
    private Boolean purchasableAlone;

    @Column(name = "main_image_url", length = 255)
    private String mainImageUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.productTypeCode == null || this.productTypeCode.isBlank()) {
            this.productTypeCode = "SIMPLE";
        }
    }

    @ManyToMany
    @JoinTable(
        name="product_sources",
        joinColumns = 
            @JoinColumn(name="product_id"),
        inverseJoinColumns =
            @JoinColumn(name="source_id")    
    )
    public List<Source> sources;

    @OneToMany(mappedBy = "product")
    public Set<ProductSource> productSources;

    @OneToMany(mappedBy = "parentProduct")
    private Set<ProductVariant> variants;

    @OneToOne(mappedBy = "variantProduct")
    private ProductVariant variantOf;

    @OneToMany(mappedBy = "parentProduct")
    private Set<ProductComponent> components;

    @OneToMany(mappedBy = "childProduct")
    private Set<ProductComponent> componentOf;

    @OneToMany(mappedBy = "product")
    private Set<ProductAddon> addons;

    @OneToMany(mappedBy = "addonProduct")
    private Set<ProductAddon> addonOf;

    @OneToMany(mappedBy = "packageProduct")
    private Set<PackageOption> packageOptions;

    @OneToMany(mappedBy = "product")
    private Set<PackageOptionItem> packageOptionItems;

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public Price getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(Price currentPrice) {
        this.currentPrice = currentPrice;
    }

    public ProductTemplate getTemplate() {
        return template;
    }

    public void setTemplate(ProductTemplate template) {
        this.template = template;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public Boolean getListable() {
        return listable;
    }

    public void setListable(Boolean listable) {
        this.listable = listable;
    }

    public Boolean getSearchable() {
        return searchable;
    }

    public void setSearchable(Boolean searchable) {
        this.searchable = searchable;
    }

    public Boolean getPurchasableAlone() {
        return purchasableAlone;
    }

    public void setPurchasableAlone(Boolean purchasableAlone) {
        this.purchasableAlone = purchasableAlone;
    }

    public String getMainImageUrl() {
        return mainImageUrl;
    }

    public void setMainImageUrl(String mainImageUrl) {
        this.mainImageUrl = mainImageUrl;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public ProductType getProductType() {
        return productType;
    }

    public void setProductType(ProductType productType) {
        this.productType = productType;
        if (productType != null) {
            this.productTypeCode = productType.getCode();
        }
    }

    public String getProductTypeCode() {
        return productTypeCode;
    }

    public void setProductTypeCode(String productTypeCode) {
        this.productTypeCode = productTypeCode;
    }

    public Set<ProductVariant> getVariants() {
        return variants;
    }

    public ProductVariant getVariantOf() {
        return variantOf;
    }

    public Set<ProductComponent> getComponents() {
        return components;
    }

    public Set<ProductComponent> getComponentOf() {
        return componentOf;
    }

    public Set<ProductAddon> getAddons() {
        return addons;
    }

    public Set<ProductAddon> getAddonOf() {
        return addonOf;
    }

    public Set<PackageOption> getPackageOptions() {
        return packageOptions;
    }

    public Set<PackageOptionItem> getPackageOptionItems() {
        return packageOptionItems;
    }
}
