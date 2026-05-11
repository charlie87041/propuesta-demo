package com.cookiesstore.infra.catalog.domain;

import com.cookiesstore.infra.shared.domain.AbstractAuditableEntity;
import com.cookiesstore.infra.shared.domain.RecordStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

@Entity
@Table(name = "cloud_providers")
public class CloudProvider extends AbstractAuditableEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RecordStatus status;

    protected CloudProvider() {
    }

    public CloudProvider(String code, String name, String description) {
        this.code = code.toLowerCase();
        this.name = name;
        this.description = description;
        this.status = RecordStatus.ACTIVE;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public RecordStatus getStatus() {
        return status;
    }
}
