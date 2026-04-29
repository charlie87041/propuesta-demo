package com.cookiesstore.admin.service.sources.pos;

import com.cookiesstore.admin.domain.pos.AdminSourcePosConfig;
import com.cookiesstore.admin.repository.pos.AdminSourcePosConfigRepository;
import com.cookiesstore.admin.web.dto.sources.pos.UpdatePosConfigForm;
import com.cookiesstore.common.repositories.CurrencyRepository;
import com.cookiesstore.common.repositories.SourceRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SourcePosConfigService {

    private final AdminSourcePosConfigRepository sourcePosConfigRepository;
    private final SourceRepository sourceRepository;
    private final CurrencyRepository currencyRepository;

    public SourcePosConfigService(
        AdminSourcePosConfigRepository sourcePosConfigRepository,
        SourceRepository sourceRepository,
        CurrencyRepository currencyRepository
    ) {
        this.sourcePosConfigRepository = sourcePosConfigRepository;
        this.sourceRepository = sourceRepository;
        this.currencyRepository = currencyRepository;
    }

    @Transactional(readOnly = true)
    public AdminSourcePosConfig getOrCreateConfig(Long sourceId) {
        var source = sourceRepository.findById(sourceId)
            .orElseThrow(() -> new SourcePosConfigSourceNotFoundException(sourceId));

        return sourcePosConfigRepository.findBySourceId(sourceId)
            .orElseGet(() -> {
                AdminSourcePosConfig config = new AdminSourcePosConfig();
                config.setSource(source);
                config.setPosEnabled(false);
                config.setClosedToday(true);
                return sourcePosConfigRepository.save(config);
            });
    }

    @Transactional(readOnly = true)
    public List<com.cookiesstore.common.entities.Currency> listActiveCurrencies() {
        return currencyRepository.findAll()
            .stream()
            .filter(com.cookiesstore.common.entities.Currency::isActive)
            .sorted(java.util.Comparator.comparing(com.cookiesstore.common.entities.Currency::getCode))
            .toList();
    }

    public AdminSourcePosConfig updateConfig(Long sourceId, UpdatePosConfigForm form) {
        AdminSourcePosConfig config = getOrCreateConfig(sourceId);

        String currencyCode = form.getDefaultCurrencyCode() == null ? null : form.getDefaultCurrencyCode().trim().toUpperCase();
        var currency = currencyRepository.findByCodeAndActiveTrue(currencyCode)
            .orElseThrow(() -> new SourcePosConfigCurrencyNotFoundException(currencyCode));

        config.setPosEnabled(form.isPosEnabled());
        config.setDefaultCurrency(currency);
        return sourcePosConfigRepository.save(config);
    }

    public UpdatePosConfigForm buildForm(Long sourceId) {
        AdminSourcePosConfig config = getOrCreateConfig(sourceId);
        UpdatePosConfigForm form = new UpdatePosConfigForm();
        form.setPosEnabled(config.isPosEnabled());
        form.setDefaultCurrencyCode(config.getDefaultCurrency() == null ? "" : config.getDefaultCurrency().getCode());
        return form;
    }
}
