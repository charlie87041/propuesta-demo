package com.cookiesstore.pos.web;

import com.cookiesstore.common.repositories.AdminSourcePosConfigRepository;
import com.cookiesstore.common.repositories.CustomerRepository;
import com.cookiesstore.pos.auth.PosPrincipal;
import com.cookiesstore.pos.dto.CloseSessionBreakdownForm;
import com.cookiesstore.pos.dto.CloseSessionForm;
import com.cookiesstore.pos.dto.StartSessionForm;
import com.cookiesstore.pos.repository.PosAdminUserRepository;
import com.cookiesstore.pos.repository.PosSourceRepository;
import com.cookiesstore.pos.services.PosCatalogService;
import com.cookiesstore.pos.services.PosOrderDomainException;
import com.cookiesstore.pos.services.PosOrderHistoryService;
import com.cookiesstore.pos.services.PosSessionCashBreakdownService;
import com.cookiesstore.pos.services.PosSessionService;

import java.util.Locale;
import java.math.RoundingMode;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Controller
public class PosWireframeController {

    private final PosSourceRepository sourceRepository;
    private final PosSessionService posSessionService;
    private final PosCatalogService posCatalogService;
    private final PosOrderHistoryService posOrderHistoryService;
    private final PosSessionCashBreakdownService posSessionCashBreakdownService;
    private final AdminSourcePosConfigRepository sourceConfigRepository;
    private final PosAdminUserRepository posAdminUserRepository;
    private final CustomerRepository customerRepository;

    public PosWireframeController(
        PosSourceRepository sourceRepository,
        PosSessionService posSessionService,
        PosCatalogService posCatalogService,
        PosOrderHistoryService posOrderHistoryService,
        PosSessionCashBreakdownService posSessionCashBreakdownService,
        AdminSourcePosConfigRepository sourceConfigRepository,
        PosAdminUserRepository posAdminUserRepository,
        CustomerRepository customerRepository
    ) {
        this.sourceRepository = sourceRepository;
        this.posSessionService = posSessionService;
        this.posCatalogService = posCatalogService;
        this.posOrderHistoryService = posOrderHistoryService;
        this.posSessionCashBreakdownService = posSessionCashBreakdownService;
        this.sourceConfigRepository = sourceConfigRepository;
        this.posAdminUserRepository = posAdminUserRepository;
        this.customerRepository = customerRepository;
    }

    @GetMapping("/{sourceId}/pos")
    public String root(@PathVariable("sourceId") Long sourceId, Authentication authentication, Model model) {
        if (posSessionService.isClosedToday(sourceId)) {
            return "redirect:/" + sourceId + "/pos/cash-drawer";
        }
        return terminal(sourceId, authentication, model);
    }

    @GetMapping("/{sourceId}/pos/terminal")
    public String terminal(@PathVariable("sourceId") Long sourceId, Authentication authentication, Model model) {
        if (posSessionService.isClosedToday(sourceId)) {
            return "redirect:/" + sourceId + "/pos/cash-drawer?closed";
        }
        enrichModel(sourceId, authentication, model);
        model.addAttribute("catalogEndpoint", "/" + sourceId + "/pos/terminal/products");
        model.addAttribute("terminalCatalog", posCatalogService.listBySource(sourceId, 0, 24));
        model.addAttribute(
            "terminalCustomers",
            customerRepository.findAll().stream()
                .filter(customer -> customer.isActive())
                .map(customer -> new TerminalCustomerView(
                    customer.getId(),
                    customer.getName(),
                    customer.getEmail(),
                    customer.getPhone() == null ? "N/A" : customer.getPhone()
                ))
                .toList()
        );
        return "pos/terminal";
    }

    @GetMapping("/{sourceId}/pos/terminal/products")
    @ResponseBody
    public Map<String, Object> terminalProducts(
        @PathVariable("sourceId") Long sourceId,
        @RequestParam(value = "offset", defaultValue = "0") int offset,
        @RequestParam(value = "limit", defaultValue = "24") int limit
    ) {
        if (posSessionService.isClosedToday(sourceId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
        var page = posCatalogService.listBySource(sourceId, offset, limit);
        return Map.of(
            "items", page.items(),
            "hasMore", page.hasMore(),
            "nextOffset", page.nextOffset()
        );
    }

    @GetMapping("/{sourceId}/pos/cash-drawer")
    public String cashDrawer(
        @PathVariable("sourceId") Long sourceId,
        Authentication authentication,
        Model model,
        @RequestParam(value = "historyPage", defaultValue = "0") int historyPage,
        @RequestParam(value = "historySize", defaultValue = "10") int historySize,
        Locale locale
    ) {
        enrichModel(sourceId, authentication, model);
        var sourceConfig = sourceConfigRepository.findBySourceId(sourceId).orElse(null);
        var currentSession = posSessionService.findTodaySession(sourceId).orElse(null);
        boolean closedToday = sourceConfig == null || sourceConfig.isClosedToday();
        boolean hasSession = currentSession != null;
        boolean hasClosedSession = hasSession && currentSession.getClosedByAdminUserId() != null;
        boolean hasOpenSession = hasSession && !hasClosedSession;
        var salesHistory = posSessionService.getSessionHistory(sourceId, historyPage, historySize, locale);
        boolean forceCashBreakdownOnClose = sourceConfig != null && sourceConfig.isForceCashBreakdownOnClose();
        String currencyCode = sourceConfig != null && sourceConfig.getDefaultCurrency() != null
            ? sourceConfig.getDefaultCurrency().getCode()
            : null;
        var currencyDenominations = posSessionCashBreakdownService.findActiveDenominationsByCurrency(currencyCode)
            .stream()
            .map(d -> new CashDenominationView(d.getId(), d.getLabel(), d.getValueMinor()))
            .toList();

        model.addAttribute("currentSession", currentSession);
        model.addAttribute("posOpeningDisabled", currentSession != null);
        model.addAttribute("posClosedToday", closedToday);
        model.addAttribute("posStatusLabelKey", hasOpenSession ? "pos.opening.status.open" : "pos.opening.status.closed");
        model.addAttribute("showCloseSessionAction", hasOpenSession);
        model.addAttribute("showClosedSessionNotice", hasClosedSession);
        model.addAttribute("closedSessionAt", hasClosedSession ? currentSession.getUpdatedAt() : null);
        model.addAttribute("closedByDisplay", resolveClosedByDisplay(currentSession));
        model.addAttribute("salesHistory", salesHistory);
        model.addAttribute("forceCashBreakdownOnClose", forceCashBreakdownOnClose);
        model.addAttribute("currencyDenominations", currencyDenominations);
        model.addAttribute("currentSessionCashPaymentsMinor", currentSession != null && currentSession.getCashPaymentsTotalMinor() != null
            ? currentSession.getCashPaymentsTotalMinor()
            : 0L);
        model.addAttribute("currentSessionOpeningCashMinor", currentSession != null && currentSession.getOpeningCashBalance() != null
            ? currentSession.getOpeningCashBalance().movePointRight(2).setScale(0, RoundingMode.DOWN).longValue()
            : 0L);
        model.addAttribute(
            "posDefaultCurrency",
            sourceConfig != null && sourceConfig.getDefaultCurrency() != null
                ? sourceConfig.getDefaultCurrency().getCode()
                : "N/A"
        );
        model.addAttribute(
            "sourceDisplayName",
            sourceRepository.findById(sourceId)
                .map(source -> source.getName())
                .orElse("Source #" + sourceId)
        );
        if (!model.containsAttribute("openForm")) {
            model.addAttribute("openForm", new StartSessionForm(null));
        }
        if (!model.containsAttribute("closeForm")) {
            if (hasSession) {
                var existingBreakdowns = posSessionCashBreakdownService.findBySession(currentSession.getId()).stream()
                    .map(row -> new CloseSessionBreakdownForm(
                        row.getDenomination().getId(),
                        row.getQuantity()
                    ))
                    .toList();
                model.addAttribute("closeForm", new CloseSessionForm(existingBreakdowns, currentSession.getDrawerNote()));
            } else {
                model.addAttribute("closeForm", new CloseSessionForm());
            }
        }
        return "pos/cash_drawer";
    }


    @GetMapping("/{sourceId}/pos/payment-methods")
    public String paymentMethods(@PathVariable("sourceId") Long sourceId, Authentication authentication, Model model) {
        enrichModel(sourceId, authentication, model);
        return "pos/payment_methods";
    }

    @GetMapping("/{sourceId}/pos/order-history")
    public String orderHistory(
        @PathVariable("sourceId") Long sourceId,
        Authentication authentication,
        Model model,
        @RequestParam(value = "page", defaultValue = "0") int page,
        @RequestParam(value = "size", defaultValue = "10") int size,
        @RequestParam(value = "search", required = false) String search,
        @RequestParam(value = "status", required = false) String status,
        Locale locale
    ) {
        enrichModel(sourceId, authentication, model);
        var history = posOrderHistoryService.getHistory(sourceId, page, size, search, status, locale);
        model.addAttribute("history", history);
        model.addAttribute("historySearch", search == null ? "" : search);
        model.addAttribute("historyStatus", status == null ? "ALL" : status.toUpperCase(Locale.ROOT));
        return "pos/order_history";
    }

    @GetMapping("/{sourceId}/pos/orders/{orderId}")
    public String orderDetails(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("orderId") Long orderId,
        Authentication authentication,
        Model model,
        Locale locale,
        RedirectAttributes redirectAttributes
    ) {
        try {
            enrichModel(sourceId, authentication, model);
            var details = posOrderHistoryService.getOrderDetails(sourceId, orderId, locale);
            model.addAttribute("orderDetails", details);
            return "pos/order_details";
        } catch (PosOrderDomainException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Order not found.");
            return "redirect:/" + sourceId + "/pos/order-history";
        }
    }

    @GetMapping("/{sourceId}/pos/session-history")
    public String sessionHistory(@PathVariable("sourceId") Long sourceId, Authentication authentication, Model model) {
        enrichModel(sourceId, authentication, model);
        return "pos/session_history";
    }

    private void enrichModel(Long sourceId, Authentication authentication, Model model) {
        PosPrincipal principal = (PosPrincipal) authentication.getPrincipal();
        boolean posClosedToday = posSessionService.isClosedToday(sourceId);
        model.addAttribute("sourceId", sourceId);
        model.addAttribute("posClosedToday", posClosedToday);
        model.addAttribute(
            "sourceCode",
            sourceRepository.findById(sourceId).map(source -> source.getCode()).orElse(null)
        );
        model.addAttribute("email", principal.email());
        model.addAttribute("userId", principal.userId());
    }

    private String resolveClosedByDisplay(com.cookiesstore.common.entities.AdminSourcePosSession currentSession) {
        if (currentSession == null || currentSession.getClosedByAdminUserId() == null) {
            return null;
        }
        Long closedById = currentSession.getClosedByAdminUserId();
        return posAdminUserRepository.findById(closedById)
            .map(adminUser -> adminUser.getEmail())
            .orElse("ID " + closedById);
    }

    private record CashDenominationView(Long id, String label, Long valueMinor) {
    }

    private record TerminalCustomerView(Long id, String name, String email, String phone) {
    }
}
