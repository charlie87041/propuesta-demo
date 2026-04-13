package com.cookiesstore.admin.service.settings.currencies;

public class CurrencyNotFoundException extends CurrencyDomainException {

    public CurrencyNotFoundException(String code) {
        super("admin.settings.currencies.flash.notFound");
    }
}

