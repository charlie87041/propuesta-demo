package com.cookiesstore.infra.deployment.domain;

import com.cookiesstore.infra.shared.domain.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "resource_outputs")
public class ResourceOutput extends AbstractAuditableEntity {

    @ManyToOne(optional = false)
    @JoinColumn(name = "deployment_run_id", nullable = false)
    private DeploymentRun deploymentRun;

    @Column(name = "resource_type", nullable = false, length = 50)
    private String resourceType;

    @Column(name = "resource_name", nullable = false, length = 150)
    private String resourceName;

    @Column(name = "output_key", nullable = false, length = 100)
    private String outputKey;

    @Column(name = "output_value", nullable = false, columnDefinition = "TEXT")
    private String outputValue;

    protected ResourceOutput() {
    }

    public ResourceOutput(
        DeploymentRun deploymentRun,
        String resourceType,
        String resourceName,
        String outputKey,
        String outputValue
    ) {
        this.deploymentRun = deploymentRun;
        this.resourceType = resourceType;
        this.resourceName = resourceName;
        this.outputKey = outputKey;
        this.outputValue = outputValue;
    }

    public DeploymentRun getDeploymentRun() {
        return deploymentRun;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public String getOutputKey() {
        return outputKey;
    }

    public String getOutputValue() {
        return outputValue;
    }
}
