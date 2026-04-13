package com.cookiesstore.common.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import java.time.Instant;

@Entity
@Table(name = "currencies")
public class Currency {

    @Id
    @Column(length = 3, nullable = false)
    private String code;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(length = 8)
    private String symbol;

    @Min(0)
    @Column(name = "fraction_digits", nullable = false)
    private int fractionDigits;

    @Column(nullable = false)
    private boolean active = true;

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

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getFractionDigits() {
        return fractionDigits;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setActive(boolean active)
    {
        this.active = active;
    }

    public void setFractionDigits(int digits)
    {
        this.fractionDigits = digits;
    }


    public void setName(String name)
    {
        this.name = name;
    }


    public void setCode(String code)
    {
        this.code = code;
    }
    public void setSymbol(String symbol)
    {
        this.symbol = symbol;
    }


}
