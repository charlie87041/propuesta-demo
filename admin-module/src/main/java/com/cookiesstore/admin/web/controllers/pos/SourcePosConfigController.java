package com.cookiesstore.admin.web.controllers.pos;

import com.cookiesstore.admin.service.sources.SourceService;
import com.cookiesstore.admin.service.sources.pos.SourcePosConfigDomainException;
import com.cookiesstore.admin.service.sources.pos.SourcePosConfigService;
import com.cookiesstore.admin.web.dto.sources.pos.UpdatePosConfigForm;
import jakarta.validation.Valid;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SourcePosConfigController {

    private final SourcePosConfigService sourcePosConfigService;
    private final SourceService sourceService;
    private final MessageSource messageSource;

    public SourcePosConfigController(
        SourcePosConfigService sourcePosConfigService,
        SourceService sourceService,
        MessageSource messageSource
    ) {
        this.sourcePosConfigService = sourcePosConfigService;
        this.sourceService = sourceService;
        this.messageSource = messageSource;
    }

    @GetMapping(
        value = "/admin/product-sources/{sourceId}/manage/pos-config",
        produces = "text/html",
        name = "admin.product-sources.manage.pos-config.view"
    )
    public String view(@PathVariable("sourceId") Long sourceId, Model model) {
        model.addAttribute("source", sourceService.getSource(sourceId));
        model.addAttribute("manageSection", "pos-config");
        model.addAttribute("currencies", sourcePosConfigService.listActiveCurrencies());
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", sourcePosConfigService.buildForm(sourceId));
        }
        return "backoffice/product-sources/manage/pos_config";
    }

    @PostMapping(
        value = "/admin/product-sources/{sourceId}/manage/pos-config",
        produces = "text/html",
        name = "admin.product-sources.manage.pos-config.update"
    )
    public String update(
        @PathVariable("sourceId") Long sourceId,
        @Valid @ModelAttribute("form") UpdatePosConfigForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("source", sourceService.getSource(sourceId));
            model.addAttribute("manageSection", "pos-config");
            model.addAttribute("currencies", sourcePosConfigService.listActiveCurrencies());
            return "backoffice/product-sources/manage/pos_config";
        }

        try {
            sourcePosConfigService.updateConfig(sourceId, form);
            redirectAttributes.addFlashAttribute("successMessage", message("admin.product_sources.manage.pos.config.flash.updated"));
        } catch (SourcePosConfigDomainException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", message("admin.product_sources.manage.pos.config.error.generic"));
        }

        return "redirect:/admin/product-sources/" + sourceId + "/manage/pos-config";
    }

    private String message(String key, Object... args) {
        return messageSource.getMessage(key, args, LocaleContextHolder.getLocale());
    }
}
