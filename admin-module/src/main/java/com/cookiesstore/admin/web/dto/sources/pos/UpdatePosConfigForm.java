package com.cookiesstore.admin.web.dto.sources.pos;

import jakarta.validation.constraints.NotBlank;

public class UpdatePosConfigForm {

    private boolean posEnabled;

    @NotBlank
    private String defaultCurrencyCode;

    public boolean isPosEnabled() {
        return posEnabled;
    }

    public void setPosEnabled(boolean posEnabled) {
        this.posEnabled = posEnabled;
    }

    public String getDefaultCurrencyCode() {
        return defaultCurrencyCode;
    }

    public void setDefaultCurrencyCode(String defaultCurrencyCode) {
        this.defaultCurrencyCode = defaultCurrencyCode;
    }
}
