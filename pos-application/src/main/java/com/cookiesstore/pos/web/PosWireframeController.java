package com.cookiesstore.pos.web;

import com.cookiesstore.pos.auth.PosPrincipal;
import com.cookiesstore.pos.repository.PosSourceConfigRepository;
import com.cookiesstore.pos.repository.PosSourceRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class PosWireframeController {

    private final PosSourceConfigRepository sourceConfigRepository;
    private final PosSourceRepository sourceRepository;

    public PosWireframeController(
        PosSourceConfigRepository sourceConfigRepository,
        PosSourceRepository sourceRepository
    ) {
        this.sourceConfigRepository = sourceConfigRepository;
        this.sourceRepository = sourceRepository;
    }

    @GetMapping("/{sourceId}/pos")
    public String root(@PathVariable("sourceId") Long sourceId, Authentication authentication, Model model) {
        return terminal(sourceId, authentication, model);
    }

    @GetMapping("/{sourceId}/pos/opening-session")
    public String openingSession(@PathVariable("sourceId") Long sourceId, Authentication authentication, Model model) {
        enrichModel(sourceId, authentication, model);
        return "pos/opening_session";
    }

    @GetMapping("/{sourceId}/pos/terminal")
    public String terminal(@PathVariable("sourceId") Long sourceId, Authentication authentication, Model model) {
        if (isClosedToday(sourceId)) {
            return "redirect:/" + sourceId + "/pos/cash-drawer?closed";
        }
        enrichModel(sourceId, authentication, model);
        return "pos/terminal";
    }

    @GetMapping("/{sourceId}/pos/cash-drawer")
    public String cashDrawer(@PathVariable("sourceId") Long sourceId, Authentication authentication, Model model) {
        enrichModel(sourceId, authentication, model);
        var sourceConfig = sourceConfigRepository.findBySourceId(sourceId).orElse(null);
        boolean closedToday = sourceConfig == null || sourceConfig.isClosedToday();
        model.addAttribute("posClosedToday", closedToday);
        model.addAttribute("posStatusLabelKey", closedToday ? "pos.opening.status.closed" : "pos.opening.status.open");
        model.addAttribute(
            "posDefaultCurrency",
            sourceConfig != null && sourceConfig.getDefaultCurrency() != null
                ? sourceConfig.getDefaultCurrency().getCode()
                : "N/A"
        );
        model.addAttribute(
            "sourceDisplayName",
            sourceConfig != null && sourceConfig.getSource() != null
                ? sourceConfig.getSource().getName()
                : "Source #" + sourceId
        );
        return "pos/cash_drawer";
    }

    @GetMapping("/{sourceId}/pos/order-history")
    public String orderHistory(@PathVariable("sourceId") Long sourceId, Authentication authentication, Model model) {
        if (isClosedToday(sourceId)) {
            return "redirect:/" + sourceId + "/pos/cash-drawer?closed";
        }
        enrichModel(sourceId, authentication, model);
        return "pos/order_history";
    }

    @GetMapping("/{sourceId}/pos/closing-session")
    public String closingSession(@PathVariable("sourceId") Long sourceId, Authentication authentication, Model model) {
        if (isClosedToday(sourceId)) {
            return "redirect:/" + sourceId + "/pos/cash-drawer?closed";
        }
        enrichModel(sourceId, authentication, model);
        return "pos/closing_session";
    }

    private void enrichModel(Long sourceId, Authentication authentication, Model model) {
        PosPrincipal principal = (PosPrincipal) authentication.getPrincipal();
        model.addAttribute("sourceId", sourceId);
        model.addAttribute(
            "sourceCode",
            sourceRepository.findById(sourceId).map(source -> source.getCode()).orElse(null)
        );
        model.addAttribute("email", principal.email());
        model.addAttribute("userId", principal.userId());
    }

    private boolean isClosedToday(Long sourceId) {
        return sourceConfigRepository.findBySourceId(sourceId)
            .map(config -> config.isClosedToday())
            .orElse(true);
    }
}
