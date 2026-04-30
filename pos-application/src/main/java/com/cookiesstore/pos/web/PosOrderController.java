package com.cookiesstore.pos.web;

import com.cookiesstore.pos.dto.CreateOrderForm;
import com.cookiesstore.pos.services.PosOrderService;
import jakarta.validation.Valid;
import java.util.Locale;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PosOrderController {

    private final PosOrderService posOrderService;
    private final MessageSource messageSource;

    public PosOrderController(PosOrderService posOrderService, MessageSource messageSource) {
        this.posOrderService = posOrderService;
        this.messageSource = messageSource;
    }

    @PostMapping(value = "/{sourceId}/pos/orders", consumes = "application/x-www-form-urlencoded")
    public String createOrder(
        @PathVariable("sourceId") Long sourceId,
        @Valid @ModelAttribute("orderForm") CreateOrderForm form,
        BindingResult bindingResult,
        @ModelAttribute("currentUserId") Long currentUserId,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", message("pos.order.create.error"));
            return "redirect:/" + sourceId + "/pos/terminal";
        }

        var order = posOrderService.createOrder(sourceId, form);
        redirectAttributes.addFlashAttribute("createdOrderId", order.getId());
        redirectAttributes.addFlashAttribute("createdOrderIncrementId", order.getIncrementId());
        return "redirect:/" + sourceId + "/pos/payment-methods";
    }



    @PostMapping(value = "/{sourceId}/pos/orders/hold", consumes = "application/x-www-form-urlencoded")
    public String holdOrder(
        @PathVariable("sourceId") Long sourceId,
        @Valid @ModelAttribute("orderForm") CreateOrderForm form,
        BindingResult bindingResult,
        @ModelAttribute("currentUserId") Long currentUserId,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", message("pos.order.hold.error"));
            return "redirect:/" + sourceId + "/pos/terminal";
        }

        var order = posOrderService.createAndHoldOrder(sourceId, form);
        redirectAttributes.addFlashAttribute("createdOrderId", order.getId());
        redirectAttributes.addFlashAttribute("createdOrderIncrementId", order.getIncrementId());
        return "redirect:/" + sourceId + "/pos/order-history";
    }

    @PostMapping("/{sourceId}/pos/orders/{orderId}/pay")
    public String payHeldOrder(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("orderId") Long orderId,
        RedirectAttributes redirectAttributes
    ) {
        var decision = posOrderService.evaluateResume(sourceId, orderId);
        if (!decision.canResumeAll()) {
            redirectAttributes.addFlashAttribute("errorMessage", message("pos.order.hold.pay.stockConflict"));
            redirectAttributes.addFlashAttribute("resumeConflictOrderId", orderId);
            redirectAttributes.addFlashAttribute("resumeConflict", decision);
            return "redirect:/" + sourceId + "/pos/order-history";
        }

        var order = posOrderService.payHeldOrder(sourceId, orderId);
        redirectAttributes.addFlashAttribute("createdOrderId", order.getId());
        redirectAttributes.addFlashAttribute("createdOrderIncrementId", order.getIncrementId());
        redirectAttributes.addFlashAttribute("successMessage", message("pos.order.hold.pay.success"));
        return "redirect:/" + sourceId + "/pos/payment-methods";
    }

    @PostMapping("/{sourceId}/pos/orders/{orderId}/cancel")
    public String cancelHeldOrder(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("orderId") Long orderId,
        RedirectAttributes redirectAttributes
    ) {
        posOrderService.cancelHeldOrder(sourceId, orderId);
        redirectAttributes.addFlashAttribute("successMessage", message("pos.order.hold.cancel.success"));
        return "redirect:/" + sourceId + "/pos/order-history";
    }

    @PostMapping("/{sourceId}/pos/orders/{orderId}/resume")
    public String resumeHeldOrder(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("orderId") Long orderId,
        RedirectAttributes redirectAttributes
    ) {
        var decision = posOrderService.evaluateResume(sourceId, orderId);
        if (decision.canResumeAll()) {
            redirectAttributes.addFlashAttribute(
                "resumedOrderDraft",
                posOrderService.resumeAllItems(sourceId, orderId)
            );
            redirectAttributes.addFlashAttribute("successMessage", message("pos.order.resume.success"));
            return "redirect:/" + sourceId + "/pos/terminal";
        }

        redirectAttributes.addFlashAttribute("resumeConflictOrderId", orderId);
        redirectAttributes.addFlashAttribute("resumeConflict", decision);
        return "redirect:/" + sourceId + "/pos/order-history";
    }

    @PostMapping("/{sourceId}/pos/orders/{orderId}/resume/available")
    public String resumeHeldOrderWithAvailable(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("orderId") Long orderId,
        RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute(
            "resumedOrderDraft",
            posOrderService.resumeWithAvailableItems(sourceId, orderId)
        );
        redirectAttributes.addFlashAttribute("successMessage", message("pos.order.resume.partial.success"));
        return "redirect:/" + sourceId + "/pos/terminal";
    }

    @PostMapping("/{sourceId}/pos/orders/{orderId}/resume/cancel")
    public String closeHeldOrderFromResumeConflict(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("orderId") Long orderId,
        RedirectAttributes redirectAttributes
    ) {
        posOrderService.cancelHeldOrder(sourceId, orderId);
        redirectAttributes.addFlashAttribute("successMessage", message("pos.order.hold.cancel.success"));
        return "redirect:/" + sourceId + "/pos/order-history";
    }

    private String message(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, args, key, locale);
    }
}
