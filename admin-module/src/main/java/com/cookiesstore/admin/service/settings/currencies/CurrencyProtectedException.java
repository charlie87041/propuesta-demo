package com.cookiesstore.admin.service.settings.currencies;

public class CurrencyProtectedException extends CurrencyDomainException {

    public CurrencyProtectedException(String code) {
        super("admin.settings.currencies.error.protected");
    }
}

