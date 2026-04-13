package com.cookiesstore.admin.web.controllers;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cookiesstore.admin.config.SettingsCurrencySearchProperties;
import com.cookiesstore.admin.search.EntitySearchSpecifications;
import com.cookiesstore.admin.service.settings.currencies.CurrencyNotFoundException;
import com.cookiesstore.admin.service.settings.currencies.CurrencyProtectedException;
import com.cookiesstore.admin.web.dto.settings.currency.CreateCurrencyForm;
import com.cookiesstore.common.entities.Currency;
import com.cookiesstore.common.repositories.CurrencyRepository;

import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.beans.factory.annotation.Value;

@Controller
@RequestMapping("/admin/settings/currencies")
public class CurrenciesController {

    private final CurrencyRepository currencyRepository;
    private final SettingsCurrencySearchProperties currencySearchProperties;
    private final MessageSource messageSource;
    private final String defaultAdminCurrency;

    public CurrenciesController(
        CurrencyRepository currencyRepository,
        SettingsCurrencySearchProperties currencySearchProperties,
        MessageSource messageSource,
        @Value("${admin.pricing.default-currency}") String defaultAdminCurrency
    ){
        this.currencyRepository = currencyRepository;
        this.currencySearchProperties = currencySearchProperties;
        this.messageSource = messageSource;
        this.defaultAdminCurrency = normalizeCode(defaultAdminCurrency);
    }
    
    @GetMapping(name="settings.currencies.list")
    public String listCurrencies(
        @PageableDefault Pageable page,
        @ModelAttribute("searchQuery") String searchQuery,
        Model model
    )
    {
        var currencies = StringUtils.hasText(searchQuery)
            ? currencyRepository.findAll( EntitySearchSpecifications.globalSearch(searchQuery, this.currencySearchProperties.getSearchableFields()), page)
            : currencyRepository.findAll(page);
        model.addAttribute("currenciesPage", currencies)  ;  
        return "backoffice/settings/currencies/list";
    }

    @GetMapping(value = "/new", name="settings.currencies.create,view")
    public String newCurrency(Model model)
    {
        if (!(model.asMap().get("form") instanceof CreateCurrencyForm)) {
            var form = new CreateCurrencyForm(null, null, null, 0, true);
            model.addAttribute("form", form);
        }
        return "backoffice/settings/currencies/form";
    }    
    
    @PostMapping(value = "/create", name="settings.currencies.create")
    public String createCurrency(
        @Valid @ModelAttribute("form") CreateCurrencyForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    )
    {
        if (bindingResult.hasErrors()) {
            return  "backoffice/settings/currencies/form";
        }
        var currency = new Currency();
        currency.setActive(form.active());
        currency.setCode(normalizeCode(form.code()));
        currency.setFractionDigits(form.fractionDigits());
        currency.setName(form.name().trim());
        currency.setSymbol(form.symbol().trim());
        currencyRepository.save(currency);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.settings.currencies.flash.created"));
        return "redirect:/admin/settings/currencies";
    } 

    @GetMapping(value = "/{code}/edit", name="settings.currencies.edit.view")
    public String editCurrency(
        @PathVariable("code") String code,
        Model model
    ) {
        String normalizedCode = normalizeCode(code);
        requireNotDefault(normalizedCode);

        Currency currency = currencyRepository.findById(normalizedCode)
            .orElseThrow(() -> new CurrencyNotFoundException(normalizedCode));
        model.addAttribute("form", new CreateCurrencyForm(
            currency.getCode(),
            currency.getName(),
            currency.getSymbol(),
            currency.getFractionDigits(),
            currency.isActive()
        ));
        model.addAttribute("currencyCode", currency.getCode());
        return "backoffice/settings/currencies/form";
    }

    @PostMapping(value = "/{code}", name="settings.currencies.update")
    public String updateCurrency(
        @PathVariable("code") String code,
        @Valid @ModelAttribute("form") CreateCurrencyForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes,
        Model model
    ) {
        String normalizedCode = normalizeCode(code);
        requireNotDefault(normalizedCode);

        if (bindingResult.hasErrors()) {
            model.addAttribute("currencyCode", normalizedCode);
            return "backoffice/settings/currencies/form";
        }

        Currency currency = currencyRepository.findById(normalizedCode)
            .orElseThrow(() -> new CurrencyNotFoundException(normalizedCode));

        currency.setActive(form.active());
        currency.setFractionDigits(form.fractionDigits());
        currency.setName(form.name().trim());
        currency.setSymbol(form.symbol().trim());
        currencyRepository.save(currency);

        redirectAttributes.addFlashAttribute("successMessage", message("admin.settings.currencies.flash.updated"));
        return "redirect:/admin/settings/currencies";
    }

    @PostMapping(value = "/{code}/delete", name="settings.currencies.delete")
    public String deleteCurrency(
        @PathVariable("code") String code,
        RedirectAttributes redirectAttributes
    ) {
        String normalizedCode = normalizeCode(code);
        requireNotDefault(normalizedCode);

        Currency currency = currencyRepository.findById(normalizedCode)
            .orElseThrow(() -> new CurrencyNotFoundException(normalizedCode));
        currencyRepository.delete(currency);
        redirectAttributes.addFlashAttribute("successMessage", message("admin.settings.currencies.flash.deleted"));
        return "redirect:/admin/settings/currencies";
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }

    private void requireNotDefault(String currencyCode) {
        if (currencyCode != null && currencyCode.equalsIgnoreCase(defaultAdminCurrency)) {
            throw new CurrencyProtectedException(currencyCode);
        }
    }

    private static String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        String trimmed = code.trim();
        return trimmed.isEmpty() ? null : trimmed.toUpperCase();
    }
}
