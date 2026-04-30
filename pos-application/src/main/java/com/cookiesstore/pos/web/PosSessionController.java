package com.cookiesstore.pos.web;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.cookiesstore.pos.dto.CloseSessionForm;
import com.cookiesstore.pos.dto.StartSessionForm;
import com.cookiesstore.pos.services.PosSessionService;

import jakarta.validation.Valid;

@Controller
public class PosSessionController {

    private final PosSessionService posSessionService;

    public PosSessionController(PosSessionService posSessionService) {
        this.posSessionService = posSessionService;
    }

    @GetMapping("/{sourceId}/pos/closing-session")
    public String closingSession(@PathVariable("sourceId") Long sourceId) {
        if (posSessionService.isClosedToday(sourceId)) {
            return "redirect:/" + sourceId + "/pos/cash-drawer?closed";
        }
        return "pos/closing_session";
    }

    @PostMapping("/{sourceId}/pos/opening-session")
    public String openingSession(
        @PathVariable("sourceId") Long sourceId,
        @Valid @ModelAttribute("openForm") StartSessionForm form,
        BindingResult bindingResult,
        @ModelAttribute("currentUserId") Long currentUserId,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("openForm", form);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.openForm", bindingResult);
            return "redirect:/" + sourceId + "/pos/cash-drawer";
        }
        posSessionService.startSession(sourceId, currentUserId, form);
        return "redirect:/" + sourceId + "/pos/cash-drawer";
    }



    @PostMapping("/{sourceId}/pos/close-session/{sessionId}")
    public String closingSession(
        @PathVariable("sourceId") Long sourceId,
        @PathVariable("sessionId") Long sessionId,
        @ModelAttribute("currentUserId") Long currentUserId,
        @Valid @ModelAttribute("closeForm") CloseSessionForm form,
        BindingResult bindingResult,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("closeForm", form);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.closeForm", bindingResult);
            return "redirect:/" + sourceId + "/pos/cash-drawer";
        }
        posSessionService.closeSession(sourceId, sessionId, currentUserId, form);
        return "redirect:/" + sourceId + "/pos/cash-drawer";
    }
}
