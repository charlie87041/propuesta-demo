package com.cookiesstore.admin.service.sources;

import java.util.Optional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.cookiesstore.admin.service.products.ProductPriceService;
import com.cookiesstore.admin.web.dto.sources.UpdateSourceProductForm;
import com.cookiesstore.common.entities.Currency;
import com.cookiesstore.common.entities.Price;
import com.cookiesstore.common.entities.ProductSource;
import com.cookiesstore.common.repositories.CurrencyRepository;
import com.cookiesstore.common.repositories.ProductSourceRepository;
import com.cookiesstore.common.repositories.SourceRepository;
import com.cookiesstore.common.util.MoneyConversion;

import jakarta.persistence.criteria.Join;
import jakarta.transaction.Transactional;

@Service
public class SourceInventoryProductService {

    private final ProductSourceRepository productSourceRepository;
    private final SourceRepository sourceRepository;
    private final CurrencyRepository currencyRepository;
    private final ProductPriceService productPriceService;

    public SourceInventoryProductService(
        ProductSourceRepository productSourceRepository,
        SourceRepository sourceRepository,
        CurrencyRepository currencyRepository,
        ProductPriceService productPriceService
    ) {
        this.productSourceRepository = productSourceRepository;
        this.sourceRepository = sourceRepository;
        this.currencyRepository = currencyRepository;
        this.productPriceService = productPriceService;
    }

    public Page<ProductSource> listBySource(Long sourceId, String searchQuery, Pageable pageable) {
        ensureSourceExists(sourceId);
        Specification<ProductSource> sourceScope = (root, query, cb) ->
            cb.equal(root.get("source").get("id"), sourceId);
        Specification<ProductSource> spec = StringUtils.hasText(searchQuery)
            ? sourceScope.and(searchByProductFields(searchQuery))
            : sourceScope;
        return productSourceRepository.findAll(spec, pageable);
    }

    public ProductSource findBySourceAndProduct(Long sourceId, Long productId) {
        ensureSourceExists(sourceId);
        return productSourceRepository.findByProductIdAndSourceId(productId, sourceId)
            .orElseThrow(() -> new SourceInventoryProductNotFoundException(sourceId, productId));
    }

    public UpdateSourceProductForm buildForm(Long sourceId, Long productId) {
        ProductSource productSource = findBySourceAndProduct(sourceId, productId);
        UpdateSourceProductForm form = new UpdateSourceProductForm();
        form.setStockQuantity(productSource.getStockQuantity());
        form.setLowStockThreshold(productSource.getLowStockThreshold());
        form.setSourceStatus(productSource.getStatus());
        if (productSource.getPrice() != null) {
            form.setSourcePrice(productSource.getPrice().getAmount());
        }
        return form;
    }

    public String formatSourcePrice(Price sourcePrice) {
        if (sourcePrice == null || sourcePrice.getAmount() == null || !StringUtils.hasText(sourcePrice.getCurrency())) {
            return "-";
        }
        String currencyCode = sourcePrice.getCurrency().trim().toUpperCase();
        int fractionDigits = currencyRepository.findById(currencyCode)
            .map(Currency::getFractionDigits)
            .orElse(2);
        String amountLabel = MoneyConversion
            .toMajor(sourcePrice.getAmountMinor(), fractionDigits)
            .setScale(Math.max(fractionDigits, 0))
            .toPlainString();
        return amountLabel + " " + currencyCode;
    }

    public Map<Long, String> formatSourcePricesByProductId(List<ProductSource> rows) {
        return rows.stream()
            .collect(Collectors.toMap(
                row -> row.getProduct().getId(),
                row -> formatSourcePrice(row.getPrice()),
                (left, right) -> left
            ));
    }

    @Transactional
    public ProductSource update(
        Long sourceId,
        Long productId,
        UpdateSourceProductForm form,
        Long actorUserId
    ) {
        ProductSource productSource = productSourceRepository.findForUpdateByProductIdAndSourceId(productId, sourceId)
            .orElseThrow(() -> new SourceInventoryProductNotFoundException(sourceId, productId));

        productSource.setStockQuantity(form.getStockQuantity());
        productSource.setLowStockThreshold(form.getLowStockThreshold());
        productSource.setStatus(form.getSourceStatus());

        Currency currency = resolveSourceCurrency(productSource.getPrice())
            .orElseGet(productPriceService::resolveDefaultCurrency);
        Price sourcePrice = productPriceService.upsertSourcePrice(
            productSource.getProduct(),
            productSource.getSource(),
            form.getSourcePrice(),
            currency,
            actorUserId
        );
        productSource.setPrice(sourcePrice);
        return productSourceRepository.save(productSource);
    }

    private Optional<Currency> resolveSourceCurrency(Price sourcePrice) {
        if (sourcePrice == null || !StringUtils.hasText(sourcePrice.getCurrency())) {
            return Optional.empty();
        }
        return currencyRepository.findByCodeAndActiveTrue(sourcePrice.getCurrency().trim().toUpperCase());
    }

    private void ensureSourceExists(Long sourceId) {
        if (!sourceRepository.existsById(sourceId)) {
            throw new SourceNotFoundException(sourceId);
        }
    }

    private Specification<ProductSource> searchByProductFields(String query) {
        return (root, criteriaQuery, cb) -> {
            String normalized = query == null ? "" : query.trim().toLowerCase();
            if (normalized.isBlank()) {
                return cb.conjunction();
            }

            Join<Object, Object> product = root.join("product");
            String pattern = "%" + normalized + "%";
            return cb.or(
                cb.like(cb.lower(product.get("name")), pattern),
                cb.like(cb.lower(product.get("sku")), pattern),
                cb.like(cb.lower(product.get("slug")), pattern)
            );
        };
    }
}
