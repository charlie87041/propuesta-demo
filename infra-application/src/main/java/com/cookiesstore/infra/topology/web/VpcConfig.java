package com.cookiesstore.infra.topology.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record VpcConfig(
    String cidrBlock,
    boolean enableDnsSupport,
    boolean enableDnsHostnames
) {
    public static VpcConfig defaults() {
        return new VpcConfig("10.0.0.0/16", true, true);
    }
}
