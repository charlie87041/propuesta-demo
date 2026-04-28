package com.cookiesstore.admin.web.dto.sources;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateSourcePurchaseOrder {

    @NotBlank
    @Size(max = 180)
    private String supplierName;

    @Size(max = 100)
    private String supplierReference;

    @NotNull
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expectedDeliveryDate;

    @NotEmpty
    @Valid
    private List<PurchaseOrderItemForm> items = new ArrayList<>();

    private String notes;

    public CreateSourcePurchaseOrder() {
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public String getSupplierReference() {
        return supplierReference;
    }

    public void setSupplierReference(String supplierReference) {
        this.supplierReference = supplierReference;
    }

    public LocalDate getExpectedDeliveryDate() {
        return expectedDeliveryDate;
    }

    public void setExpectedDeliveryDate(LocalDate expectedDeliveryDate) {
        this.expectedDeliveryDate = expectedDeliveryDate;
    }

    public List<PurchaseOrderItemForm> getItems() {
        return items;
    }

    public void setItems(List<PurchaseOrderItemForm> items) {
        this.items = items == null ? new ArrayList<>() : items;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
