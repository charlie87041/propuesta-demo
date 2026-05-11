package com.cookiesstore.infra.catalog.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ProjectForm {

    @NotBlank
    @Size(max = 100)
    private String key;

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 500)
    private String description;

    @NotBlank
    @Size(max = 50)
    private String defaultRegion = "us-east-1";

    @Size(max = 150)
    private String owner;

    @NotBlank
    private String providerCode = "aws";

    @NotBlank
    private String providerCredentialsJson = """
        {
          "accessKeyId": "test",
          "secretAccessKey": "test",
          "region": "us-east-1"
        }
        """;

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

    public String getProviderCode() {
        return providerCode;
    }

    public void setProviderCode(String providerCode) {
        this.providerCode = providerCode;
    }

    public String getProviderCredentialsJson() {
        return providerCredentialsJson;
    }

    public void setProviderCredentialsJson(String providerCredentialsJson) {
        this.providerCredentialsJson = providerCredentialsJson;
    }
}
