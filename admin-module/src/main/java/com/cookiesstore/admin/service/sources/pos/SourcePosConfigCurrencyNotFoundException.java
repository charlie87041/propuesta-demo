package com.cookiesstore.admin.service.sources.pos;

public class SourcePosConfigCurrencyNotFoundException extends SourcePosConfigDomainException {

    public SourcePosConfigCurrencyNotFoundException(String currencyCode) {
        super("Currency not found or inactive: " + currencyCode);
    }
}
