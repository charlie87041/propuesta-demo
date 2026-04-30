package com.cookiesstore.pos.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class CreateOrderForm {

    private Long resumeOrderId;

    private Long customerId;

    @NotBlank
    private String currencyCode;

    @Valid
    @NotEmpty
    private List<CreateOrderLineForm> lines = new ArrayList<>();

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public Long getResumeOrderId() {
        return resumeOrderId;
    }

    public void setResumeOrderId(Long resumeOrderId) {
        this.resumeOrderId = resumeOrderId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public List<CreateOrderLineForm> getLines() {
        return lines;
    }

    public void setLines(List<CreateOrderLineForm> lines) {
        this.lines = lines;
    }
}
