package com.cookiesstore.common.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Set;

@Entity
@Table(
    name = "orders",
    indexes = {
        @Index(name = "idx_orders_customer_id", columnList = "customer_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_created_at", columnList = "created_at")
    }
)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "increment_id", nullable = false, unique = true, length = 40)
    private String incrementId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(name = "is_guest", nullable = false)
    private boolean guest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @Column(name = "customer_email", length = 255)
    private String customerEmail;

    @Column(name = "customer_first_name", length = 120)
    private String customerFirstName;

    @Column(name = "customer_last_name", length = 120)
    private String customerLastName;

    @Column(name = "coupon_code", length = 100)
    private String couponCode;

    @Column(name = "is_gift", nullable = false)
    private boolean gift;

    @Column(name = "total_item_count", nullable = false)
    private Integer totalItemCount = 0;

    @Column(name = "total_qty_ordered", nullable = false)
    private Integer totalQtyOrdered = 0;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_currency_code", nullable = false)
    private Currency orderCurrency;

    @Column(name = "sub_total_minor", nullable = false)
    private Long subTotalMinor = 0L;

    @Column(name = "discount_total_minor", nullable = false)
    private Long discountTotalMinor = 0L;

    @Column(name = "tax_total_minor", nullable = false)
    private Long taxTotalMinor = 0L;

    @Column(name = "shipping_total_minor", nullable = false)
    private Long shippingTotalMinor = 0L;

    @Column(name = "grand_total_minor", nullable = false)
    private Long grandTotalMinor = 0L;

    @OneToMany(mappedBy = "order")
    private Set<OrderAddress> addresses;

    @OneToMany(mappedBy = "order")
    private Set<OrderItem> items;

    @OneToMany(mappedBy = "order")
    private Set<OrderStatusHistory> statusHistory;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

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
        return id;
    }

    public String getIncrementId() {
        return incrementId;
    }

    public void setIncrementId(String incrementId) {
        this.incrementId = incrementId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public boolean isGuest() {
        return guest;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getCustomerFirstName() {
        return customerFirstName;
    }

    public void setCustomerFirstName(String customerFirstName) {
        this.customerFirstName = customerFirstName;
    }

    public String getCustomerLastName() {
        return customerLastName;
    }

    public void setCustomerLastName(String customerLastName) {
        this.customerLastName = customerLastName;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public void setCouponCode(String couponCode) {
        this.couponCode = couponCode;
    }

    public boolean isGift() {
        return gift;
    }

    public void setGift(boolean gift) {
        this.gift = gift;
    }

    public Integer getTotalItemCount() {
        return totalItemCount;
    }

    public void setTotalItemCount(Integer totalItemCount) {
        this.totalItemCount = totalItemCount;
    }

    public Integer getTotalQtyOrdered() {
        return totalQtyOrdered;
    }

    public void setTotalQtyOrdered(Integer totalQtyOrdered) {
        this.totalQtyOrdered = totalQtyOrdered;
    }

    public Currency getOrderCurrency() {
        return orderCurrency;
    }

    public void setOrderCurrency(Currency orderCurrency) {
        this.orderCurrency = orderCurrency;
    }

    public Long getSubTotalMinor() {
        return subTotalMinor;
    }

    public void setSubTotalMinor(Long subTotalMinor) {
        this.subTotalMinor = subTotalMinor;
    }

    public Long getDiscountTotalMinor() {
        return discountTotalMinor;
    }

    public void setDiscountTotalMinor(Long discountTotalMinor) {
        this.discountTotalMinor = discountTotalMinor;
    }

    public Long getTaxTotalMinor() {
        return taxTotalMinor;
    }

    public void setTaxTotalMinor(Long taxTotalMinor) {
        this.taxTotalMinor = taxTotalMinor;
    }

    public Long getShippingTotalMinor() {
        return shippingTotalMinor;
    }

    public void setShippingTotalMinor(Long shippingTotalMinor) {
        this.shippingTotalMinor = shippingTotalMinor;
    }

    public Long getGrandTotalMinor() {
        return grandTotalMinor;
    }

    public void setGrandTotalMinor(Long grandTotalMinor) {
        this.grandTotalMinor = grandTotalMinor;
    }

    public Set<OrderAddress> getAddresses() {
        return addresses;
    }

    public Set<OrderItem> getItems() {
        return items;
    }

    public Set<OrderStatusHistory> getStatusHistory() {
        return statusHistory;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
