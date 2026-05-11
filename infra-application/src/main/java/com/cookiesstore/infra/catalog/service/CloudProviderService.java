package com.cookiesstore.infra.catalog.service;

import com.cookiesstore.infra.catalog.domain.CloudProvider;
import com.cookiesstore.infra.catalog.repository.CloudProviderRepository;
import com.cookiesstore.infra.shared.domain.RecordStatus;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CloudProviderService {

    private final CloudProviderRepository cloudProviderRepository;

    public CloudProviderService(CloudProviderRepository cloudProviderRepository) {
        this.cloudProviderRepository = cloudProviderRepository;
    }

    public List<CloudProvider> list() {
        return cloudProviderRepository.findAll();
    }

    public List<CloudProvider> listActive() {
        return cloudProviderRepository.findAll()
            .stream()
            .filter(provider -> provider.getStatus() == RecordStatus.ACTIVE)
            .toList();
    }

    public CloudProvider requireActiveByCode(String code) {
        return cloudProviderRepository.findByCodeIgnoreCase(code)
            .filter(provider -> provider.getStatus() == RecordStatus.ACTIVE)
            .orElseThrow(() -> new IllegalArgumentException("Provider not available: " + code));
    }

    @Transactional
    public CloudProvider createIfMissing(String code, String name, String description) {
        return cloudProviderRepository.findByCodeIgnoreCase(code)
            .orElseGet(() -> cloudProviderRepository.save(new CloudProvider(code, name, description)));
    }
}
