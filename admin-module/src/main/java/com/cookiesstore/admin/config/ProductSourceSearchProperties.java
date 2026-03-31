package com.cookiesstore.admin.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "admin.search.product-sources")
public class ProductSourceSearchProperties {

    private List<String> searchableFields = List.of("name", "code", "description");

    public List<String> getSearchableFields() {
        return searchableFields;
    }

    public void setSearchableFields(List<String> searchableFields) {
        this.searchableFields = searchableFields;
    }

    public List<String> getFields() {
        return searchableFields;
    }

    public void setFields(List<String> fields) {
        this.searchableFields = fields;
    }
}
