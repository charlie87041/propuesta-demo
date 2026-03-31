package com.cookiesstore.common.seeder;

import com.cookiesstore.common.entities.Source;
import com.cookiesstore.common.repositories.SourceRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DefaultSourceSeeder implements CommandLineRunner {

    private static final String DEFAULT_SOURCE_CODE = "default";
    private static final String DEFAULT_SOURCE_NAME = "Default Source";
    private static final String DEFAULT_SOURCE_DESCRIPTION = "System-managed fallback source";

    private final SourceRepository sourceRepository;

    public DefaultSourceSeeder(SourceRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seed();
    }

    @Transactional
    public void seed() {
        Source existing = sourceRepository.findByCode(DEFAULT_SOURCE_CODE).orElse(null);
        if (existing == null) {
            Source created = new Source();
            created.setCode(DEFAULT_SOURCE_CODE);
            created.setName(DEFAULT_SOURCE_NAME);
            created.setDescription(DEFAULT_SOURCE_DESCRIPTION);
            created.setActive(true);
            created.setSystemManaged(true);
            sourceRepository.save(created);
            return;
        }

        if (!existing.isSystemManaged()) {
            existing.setSystemManaged(true);
            sourceRepository.save(existing);
        }
    }
}
