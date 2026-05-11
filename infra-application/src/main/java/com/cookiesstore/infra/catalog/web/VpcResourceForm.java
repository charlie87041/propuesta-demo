package com.cookiesstore.infra.catalog.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class VpcResourceForm {

    @NotBlank
    @Size(max = 150)
    private String name;

    @Size(max = 500)
    private String description;

    @NotBlank
    @Size(max = 20)
    @Pattern(regexp = "^\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}/\\d{1,2}$",
             message = "Must be a valid CIDR block (e.g. 10.0.0.0/16)")
    private String cidrBlock = "10.0.0.0/16";

    private boolean enableDnsSupport = true;

    private boolean enableDnsHostnames = true;

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

    public String getCidrBlock() {
        return cidrBlock;
    }

    public void setCidrBlock(String cidrBlock) {
        this.cidrBlock = cidrBlock;
    }

    public boolean isEnableDnsSupport() {
        return enableDnsSupport;
    }

    public void setEnableDnsSupport(boolean enableDnsSupport) {
        this.enableDnsSupport = enableDnsSupport;
    }

    public boolean isEnableDnsHostnames() {
        return enableDnsHostnames;
    }

    public void setEnableDnsHostnames(boolean enableDnsHostnames) {
        this.enableDnsHostnames = enableDnsHostnames;
    }
}
