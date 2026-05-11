package com.cookiesstore.infra.catalog.web;

import com.cookiesstore.infra.catalog.domain.ProjectEnvironment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProjectEnvironmentForm {

    @NotBlank
    @Size(max = 50)
    private String name;

    @Size(max = 50)
    private String region;

    @Size(max = 255)
    private String domain;

    public static ProjectEnvironmentForm from(ProjectEnvironment environment) {
        ProjectEnvironmentForm form = new ProjectEnvironmentForm();
        form.setName(environment.getName());
        form.setRegion(environment.getRegion());
        form.setDomain(environment.getDomain());
        return form;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
