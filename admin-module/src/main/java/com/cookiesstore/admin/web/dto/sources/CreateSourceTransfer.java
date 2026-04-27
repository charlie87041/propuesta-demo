package com.cookiesstore.admin.web.dto.sources;

import java.util.ArrayList;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class CreateSourceTransfer {

    @NotNull
    private Long destinationSourceId;

    @NotNull
    @Size(min = 1)
    @Valid
    private List<SourceTransferItem> items = new ArrayList<>();

    private Integer totalItems;
    private Integer totalQuantity;
    private Integer transitTime;
    private String weightClass;
    private String notes;

    public CreateSourceTransfer() {
    }

    public Long getDestinationSourceId() {
        return destinationSourceId;
    }

    public void setDestinationSourceId(Long destinationSourceId) {
        this.destinationSourceId = destinationSourceId;
    }

    public List<SourceTransferItem> getItems() {
        return items;
    }

    public void setItems(List<SourceTransferItem> items) {
        this.items = items == null ? new ArrayList<>() : items;
    }

    public Integer getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(Integer totalItems) {
        this.totalItems = totalItems;
    }

    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(Integer totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public Integer getTransitTime() {
        return transitTime;
    }

    public void setTransitTime(Integer transitTime) {
        this.transitTime = transitTime;
    }

    public String getWeightClass() {
        return weightClass;
    }

    public void setWeightClass(String weightClass) {
        this.weightClass = weightClass;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
