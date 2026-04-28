package com.cookiesstore.admin.web.dto.sources;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

public class ResolvePurchaseOrderAdhocProductsForm {

    @NotEmpty
    @Valid
    private List<ResolvePurchaseOrderAdhocItemForm> items = new ArrayList<>();

    public ResolvePurchaseOrderAdhocProductsForm() {
    }

    public List<ResolvePurchaseOrderAdhocItemForm> getItems() {
        return items;
    }

    public void setItems(List<ResolvePurchaseOrderAdhocItemForm> items) {
        this.items = items == null ? new ArrayList<>() : items;
    }
}
