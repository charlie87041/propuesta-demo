package com.cookiesstore.admin.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "admin.search.customers")
public class CustomerSearchProperties {

    private List<String> searchableFields = List.of("name");

    public List<String> getSearchableFields() {
        return searchableFields;
    }

    public void setSearchableFields(List<String> searchableFields) {
        this.searchableFields = searchableFields;
    }
}
