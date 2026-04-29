package com.cookiesstore.admin.service.sources.pos;

public class SourcePosConfigSourceNotFoundException extends SourcePosConfigDomainException {

    public SourcePosConfigSourceNotFoundException(Long sourceId) {
        super("Source not found: " + sourceId);
    }
}
