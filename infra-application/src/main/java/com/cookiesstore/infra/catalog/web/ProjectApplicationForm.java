package com.cookiesstore.infra.catalog.web;

import com.cookiesstore.infra.catalog.domain.ApplicationBuildType;
import com.cookiesstore.infra.catalog.domain.ApplicationServiceType;
import com.cookiesstore.infra.catalog.domain.ProjectApplication;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProjectApplicationForm {

    @NotBlank
    private String projectId;

    @NotBlank
    @Size(max = 100)
    private String key;

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 500)
    private String description;

    @NotNull
    private ApplicationBuildType buildType;

    @NotNull
    private ApplicationServiceType serviceType;

    @Size(max = 100)
    private String runtime;

    @Size(max = 255)
    private String sourceLocation;

    @Min(1)
    @Max(65535)
    private Integer defaultPort;

    public static ProjectApplicationForm from(ProjectApplication application) {
        ProjectApplicationForm form = new ProjectApplicationForm();
        form.setProjectId(application.getProject().getId());
        form.setKey(application.getKey());
        form.setName(application.getName());
        form.setDescription(application.getDescription());
        form.setBuildType(application.getBuildType());
        form.setServiceType(application.getServiceType());
        form.setRuntime(application.getRuntime());
        form.setSourceLocation(application.getSourceLocation());
        form.setDefaultPort(application.getDefaultPort());
        return form;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public ApplicationBuildType getBuildType() {
        return buildType;
    }

    public void setBuildType(ApplicationBuildType buildType) {
        this.buildType = buildType;
    }

    public ApplicationServiceType getServiceType() {
        return serviceType;
    }

    public void setServiceType(ApplicationServiceType serviceType) {
        this.serviceType = serviceType;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getSourceLocation() {
        return sourceLocation;
    }

    public void setSourceLocation(String sourceLocation) {
        this.sourceLocation = sourceLocation;
    }

    public Integer getDefaultPort() {
        return defaultPort;
    }

    public void setDefaultPort(Integer defaultPort) {
        this.defaultPort = defaultPort;
    }
}
