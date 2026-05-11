package com.cookiesstore.infra.catalog.web;

import com.cookiesstore.infra.catalog.domain.Project;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EditProjectForm {

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 500)
    private String description;

    @NotBlank
    @Size(max = 50)
    private String defaultRegion;

    @Size(max = 150)
    private String owner;

    public static EditProjectForm from(Project project) {
        EditProjectForm form = new EditProjectForm();
        form.setName(project.getName());
        form.setDescription(project.getDescription());
        form.setDefaultRegion(project.getDefaultRegion());
        form.setOwner(project.getOwner());
        return form;
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

    public String getDefaultRegion() {
        return defaultRegion;
    }

    public void setDefaultRegion(String defaultRegion) {
        this.defaultRegion = defaultRegion;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
