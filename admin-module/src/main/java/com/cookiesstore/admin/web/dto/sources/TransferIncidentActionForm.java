package com.cookiesstore.admin.web.dto.sources;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class TransferIncidentActionForm {

    @NotBlank
    private String description;

    @NotEmpty
    @Valid
    private List<TransferIncidentItemForm> items = new ArrayList<>();

    public TransferIncidentActionForm() {
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<TransferIncidentItemForm> getItems() {
        return items;
    }

    public void setItems(List<TransferIncidentItemForm> items) {
        this.items = items == null ? new ArrayList<>() : items;
    }
}
