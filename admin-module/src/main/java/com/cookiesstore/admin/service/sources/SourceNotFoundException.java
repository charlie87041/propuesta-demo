package com.cookiesstore.admin.service.sources;

import java.util.List;

public class SourceNotFoundException extends SourceDomainException {

    public SourceNotFoundException(Long sourceId) {
        super("admin.sources.flash.notFound");
    }

     public SourceNotFoundException(List<Long> sourceId) {
        super("admin.sources.flash.notFound");
    }
}
