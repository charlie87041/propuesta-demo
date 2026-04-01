package com.cookiesstore.common.seeder;

import com.cookiesstore.common.entities.ProductType;
import com.cookiesstore.common.repositories.ProductTypeRepository;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(15)
public class ProductTypeSeeder implements CommandLineRunner {

    private final ProductTypeRepository productTypeRepository;

    public ProductTypeSeeder(ProductTypeRepository productTypeRepository) {
        this.productTypeRepository = productTypeRepository;
    }

    @Override
    @Transactional
    public void run(String... args) {
        seed();
    }

    @Transactional
    public void seed() {
        List<ProductTypeDefinition> definitions = List.of(
            new ProductTypeDefinition("SIMPLE", "Simple", "Standalone sellable product", false, false, true, true, true, true),
            new ProductTypeDefinition("BUNDLE", "Bundle", "Composable product made of multiple child products", true, false, true, true, true, true),
            new ProductTypeDefinition("PACKAGE", "Package", "Fixed commercial pack sold as one unit", true, false, true, true, true, true),
            new ProductTypeDefinition("VARIANT_PARENT", "Variant Parent", "Non-sellable parent that groups variants", false, true, false, true, true, false),
            new ProductTypeDefinition("VARIANT", "Variant", "Sellable variant linked to a variant parent", false, false, true, false, false, true),
            new ProductTypeDefinition("ADD_ON", "Add On", "Complementary product that is not sold on its own by default", false, false, false, false, false, false)
        );

        for (ProductTypeDefinition definition : definitions) {
            ProductType type = productTypeRepository.findById(definition.code()).orElseGet(ProductType::new);
            type.setCode(definition.code());
            type.setName(definition.name());
            type.setDescription(definition.description());
            type.setSupportsComponents(definition.supportsComponents());
            type.setSupportsVariants(definition.supportsVariants());
            type.setSupportsAddons(definition.supportsAddons());
            type.setDefaultIsListable(definition.defaultIsListable());
            type.setDefaultIsSearchable(definition.defaultIsSearchable());
            type.setDefaultIsPurchasableAlone(definition.defaultIsPurchasableAlone());
            productTypeRepository.save(type);
        }
    }

    private record ProductTypeDefinition(
        String code,
        String name,
        String description,
        boolean supportsComponents,
        boolean supportsVariants,
        boolean supportsAddons,
        boolean defaultIsListable,
        boolean defaultIsSearchable,
        boolean defaultIsPurchasableAlone
    ) {
    }
}
