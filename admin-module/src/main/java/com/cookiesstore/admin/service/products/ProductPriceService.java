package com.cookiesstore.admin.service.products;

import com.cookiesstore.admin.config.PricingProperties;
import com.cookiesstore.admin.service.products.validation.PriceValidationSupport;
import com.cookiesstore.common.entities.Currency;
import com.cookiesstore.common.entities.Price;
import com.cookiesstore.common.entities.Product;
import com.cookiesstore.common.entities.Source;
import com.cookiesstore.common.repositories.CurrencyRepository;
import com.cookiesstore.common.repositories.PriceRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ProductPriceService {

    private final PriceRepository priceRepository;
    private final CurrencyRepository currencyRepository;
    private final PricingProperties pricingProperties;

    public ProductPriceService(
        PriceRepository priceRepository,
        CurrencyRepository currencyRepository,
        PricingProperties pricingProperties
    ) {
        this.priceRepository = priceRepository;
        this.currencyRepository = currencyRepository;
        this.pricingProperties = pricingProperties;
    }

    public Currency resolveDefaultCurrency() {
        String configuredCurrency = trimToNull(pricingProperties.getDefaultCurrency());
        if (configuredCurrency == null || configuredCurrency.length() != 3) {
            throw new ProductPriceValidationException("admin.products.error.currency.invalid");
        }
        return requireActiveCurrency(configuredCurrency.toUpperCase());
    }

    public void upsertProductCurrentPrice(Product product, BigDecimal amount, Currency currency, Long actorUserId) {
        if (amount == null) {
            return;
        }

        Price openBasePrice = priceRepository
            .findFirstByProductIdAndSourceIdIsNullAndCurrencyAndValidToIsNull(product.getId(), currency.getCode())
            .orElse(null);

        BigDecimal requestedAmount = normalizeAmount(amount, currency);
        long requestedAmountMinor = PriceValidationSupport.toMinorOrThrow(requestedAmount, currency.getFractionDigits());
        if (openBasePrice != null && openBasePrice.getAmountMinor() == requestedAmountMinor) {
            product.setCurrentPrice(openBasePrice);
            return;
        }

        if (openBasePrice != null) {
            openBasePrice.setValidTo(Instant.now());
            priceRepository.save(openBasePrice);
        }

        Price currentPrice = newProductPrice(product, amount, currency, actorUserId);
        product.setCurrentPrice(currentPrice);
    }

    public Price upsertSourcePrice(
        Product product,
        Source source,
        BigDecimal amount,
        Currency currency,
        Long actorUserId
    ) {
        Price openSourcePrice = priceRepository
            .findFirstByProductIdAndSourceIdAndCurrencyAndValidToIsNull(product.getId(), source.getId(), currency.getCode())
            .orElse(null);

        if (amount == null) {
            if (openSourcePrice != null) {
                openSourcePrice.setValidTo(Instant.now());
                priceRepository.save(openSourcePrice);
            }
            return null;
        }

        BigDecimal requestedAmount = normalizeAmount(amount, currency);
        if (openSourcePrice != null
            && openSourcePrice.getAmountMinor() == PriceValidationSupport.toMinorOrThrow(requestedAmount, currency.getFractionDigits())) {
            return openSourcePrice;
        }

        if (openSourcePrice != null) {
            openSourcePrice.setValidTo(Instant.now());
            priceRepository.save(openSourcePrice);
        }

        Price price = new Price();
        price.setSource(source);
        applyAmount(price, requestedAmount, currency);
        price.setCurrency(currency.getCode());
        price.setProduct(product);
        price.setValidFrom(Instant.now());
        price.setCreatedBy(actorUserId);
        return priceRepository.save(price);
    }

    private Currency requireActiveCurrency(String currencyCode) {
        String normalizedCurrencyCode = trimToNull(currencyCode);
        if (normalizedCurrencyCode == null || normalizedCurrencyCode.length() != 3) {
            throw new ProductPriceValidationException("admin.products.error.currency.invalid");
        }
        String upperCaseCode = normalizedCurrencyCode.toUpperCase();
        return currencyRepository.findByCodeAndActiveTrue(upperCaseCode)
            .orElseThrow(() -> new ProductPriceValidationException("admin.products.error.currency.invalid"));
    }

    private BigDecimal normalizeAmount(BigDecimal amount, Currency currency) {
        int resolvedScale = Math.max(currency.getFractionDigits(), 0);
        return amount.setScale(resolvedScale, RoundingMode.HALF_UP);
    }

    private void applyAmount(Price price, BigDecimal amount, Currency currency) {
        int fractionDigits = Math.max(currency.getFractionDigits(), 0);
        BigDecimal normalizedAmount = normalizeAmount(amount, currency);
        price.setAmount(normalizedAmount);
        price.setAmountMinor(PriceValidationSupport.toMinorOrThrow(normalizedAmount, fractionDigits));
    }

    private Price newProductPrice(Product product, BigDecimal amount, Currency currency, Long actorUserId) {
        Price price = new Price();
        applyAmount(price, amount, currency);
        price.setCurrency(currency.getCode());
        price.setProduct(product);
        price.setValidFrom(Instant.now());
        price.setCreatedBy(actorUserId);
        return priceRepository.save(price);
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }
}
