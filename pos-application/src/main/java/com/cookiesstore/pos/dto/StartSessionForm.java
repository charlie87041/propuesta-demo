package com.cookiesstore.pos.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class StartSessionForm {

    @NotNull
    @DecimalMin("1.0")
    private BigDecimal inCashAmount;

    public StartSessionForm() {
    }

    public StartSessionForm(BigDecimal inCashAmount) {
        this.inCashAmount = inCashAmount;
    }

    public BigDecimal getInCashAmount() {
        return inCashAmount;
    }

    public void setInCashAmount(BigDecimal inCashAmount) {
        this.inCashAmount = inCashAmount;
    }
}
