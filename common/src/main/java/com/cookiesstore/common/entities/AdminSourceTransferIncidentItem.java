package com.cookiesstore.common.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(
    name = "admin_source_transfer_incident_items",
    indexes = {
        @Index(name = "idx_admin_source_transfer_incident_items_incident", columnList = "incident_id"),
        @Index(name = "idx_admin_source_transfer_incident_items_product", columnList = "product_id")
    }
)
public class AdminSourceTransferIncidentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "incident_id", nullable = false)
    private AdminSourceTransferIncident incident;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @Column(name = "product_name", nullable = false, length = 180)
    private String productName;

    @Column(name = "expected_quantity", nullable = false)
    private Integer expectedQuantity;

    @Column(name = "missing_quantity", nullable = false)
    private Integer missingQuantity;

    @Column(name = "received_quantity", nullable = false)
    private Integer receivedQuantity;

    @Column(nullable = false)
    private boolean archived = false;

    @Column(name = "closed_at")
    private Instant closedAt;

    @Column(name = "closed_by_admin_user_id")
    private Long closedByAdminUserId;

    @Column(name = "reverted_at")
    private Instant revertedAt;

    @Column(name = "reverted_by_admin_user_id")
    private Long revertedByAdminUserId;

    public Long getId() {
        return id;
    }

    public AdminSourceTransferIncident getIncident() {
        return incident;
    }

    public void setIncident(AdminSourceTransferIncident incident) {
        this.incident = incident;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getExpectedQuantity() {
        return expectedQuantity;
    }

    public void setExpectedQuantity(Integer expectedQuantity) {
        this.expectedQuantity = expectedQuantity;
    }

    public Integer getMissingQuantity() {
        return missingQuantity;
    }

    public void setMissingQuantity(Integer missingQuantity) {
        this.missingQuantity = missingQuantity;
    }

    public Integer getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(Integer receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public Instant getClosedAt() {
        return closedAt;
    }

    public void setClosedAt(Instant closedAt) {
        this.closedAt = closedAt;
    }

    public Long getClosedByAdminUserId() {
        return closedByAdminUserId;
    }

    public void setClosedByAdminUserId(Long closedByAdminUserId) {
        this.closedByAdminUserId = closedByAdminUserId;
    }

    public Instant getRevertedAt() {
        return revertedAt;
    }

    public void setRevertedAt(Instant revertedAt) {
        this.revertedAt = revertedAt;
    }

    public Long getRevertedByAdminUserId() {
        return revertedByAdminUserId;
    }

    public void setRevertedByAdminUserId(Long revertedByAdminUserId) {
        this.revertedByAdminUserId = revertedByAdminUserId;
    }
}
