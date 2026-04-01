package com.cookiesstore.common.seeder;

import com.cookiesstore.common.entities.PackageOptionType;
import com.cookiesstore.common.repositories.PackageOptionTypeRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(16)
public class PackageOptionTypeSeeder implements CommandLineRunner {

    private final PackageOptionTypeRepository packageOptionTypeRepository;

    public PackageOptionTypeSeeder(PackageOptionTypeRepository packageOptionTypeRepository) {
        this.packageOptionTypeRepository = packageOptionTypeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seed();
    }

    @Transactional
    public void seed() {
        List<PackageOptionTypeDefinition> definitions = List.of(
            new PackageOptionTypeDefinition("ALL_OF", "All Of", "All items in the option are selected by default", false),
            new PackageOptionTypeDefinition("ONE_OF", "One Of", "Exactly one item can be selected", true),
            new PackageOptionTypeDefinition("N_OF", "N Of", "A fixed or bounded number of items can be selected", true)
        );

        for (PackageOptionTypeDefinition definition : definitions) {
            PackageOptionType optionType = packageOptionTypeRepository.findByCode(definition.code())
                .orElseGet(PackageOptionType::new);
            optionType.setCode(definition.code());
            optionType.setName(definition.name());
            optionType.setDescription(definition.description());
            optionType.setUsesSelectionBounds(definition.usesSelectionBounds());
            packageOptionTypeRepository.save(optionType);
        }
    }

    private record PackageOptionTypeDefinition(
        String code,
        String name,
        String description,
        boolean usesSelectionBounds
    ) {
    }
}
