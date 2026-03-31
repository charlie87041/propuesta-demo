package com.cookiesstore.admin.service.sources;

import com.cookiesstore.admin.service.sources.SourceCodeExistsException;
import com.cookiesstore.admin.service.sources.SourceNotFoundException;
import com.cookiesstore.admin.service.sources.SourceSystemManagedException;
import com.cookiesstore.admin.service.sources.SourceUniqueConstraintException;
import com.cookiesstore.admin.web.dto.sources.CreateProductSourceForm;
import com.cookiesstore.common.entities.Source;
import com.cookiesstore.common.repositories.SourceRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional
public class SourceService {

    private final SourceRepository sourceRepository;

    public SourceService(SourceRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    @Transactional(readOnly = true)
    public Source getSource(Long sourceId) {
        return sourceRepository.findById(sourceId)
            .orElseThrow(() -> new SourceNotFoundException(sourceId));
    }

    public Source createSource(CreateProductSourceForm form) {
        String code = form.code().trim();
        if (sourceRepository.existsByCode(code)) {
            throw new SourceCodeExistsException(code);
        }

        Source source = new Source();
        source.setCode(code);
        source.setName(form.name().trim());
        source.setDescription(trimToNull(form.description()));
        source.setActive(form.active());
        source.setSystemManaged(false);

        try {
            return sourceRepository.save(source);
        } catch (DataIntegrityViolationException ex) {
            throw new SourceUniqueConstraintException();
        }
    }

    public Source updateSource(Long sourceId, CreateProductSourceForm form) {
        Source source = sourceRepository.findById(sourceId)
            .orElseThrow(() -> new SourceNotFoundException(sourceId));

        if (source.isSystemManaged()) {
            throw new SourceSystemManagedException(sourceId);
        }

        String code = form.code().trim();
        var existingByCode = sourceRepository.findByCode(code);
        if (existingByCode.isPresent() && !existingByCode.get().getId().equals(sourceId)) {
            throw new SourceCodeExistsException(code);
        }

        source.setCode(code);
        source.setName(form.name().trim());
        source.setDescription(trimToNull(form.description()));
        source.setActive(form.active());

        try {
            return sourceRepository.save(source);
        } catch (DataIntegrityViolationException ex) {
            throw new SourceUniqueConstraintException();
        }
    }

    public void deleteSource(Long sourceId) {
        Source source = sourceRepository.findById(sourceId)
            .orElseThrow(() -> new SourceNotFoundException(sourceId));
        if (source.isSystemManaged()) {
            throw new SourceSystemManagedException(sourceId);
        }
        sourceRepository.delete(source);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
