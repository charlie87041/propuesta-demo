package com.cookiesstore.pos.web.advice.model;

import com.cookiesstore.pos.repository.PosSourceRepository;
import com.cookiesstore.pos.web.PosOrderController;
import com.cookiesstore.pos.web.advice.support.BaseAdviceSupport;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@ControllerAdvice(assignableTypes = PosOrderController.class)
public class PosOrderModelAdvice extends BaseAdviceSupport {

    private final PosSourceRepository sourceRepository;

    public PosOrderModelAdvice(PosSourceRepository sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    @ModelAttribute
    public void populate(Model model, @PathVariable(value = "sourceId", required = false) Long sourceId) {
        var principal = currentPrincipal();
        model.addAttribute("currentUserId", principal.userId());
        model.addAttribute("userId", principal.userId());
        model.addAttribute("email", principal.email());
        model.addAttribute("sourceId", sourceId);
        model.addAttribute(
            "sourceCode",
            sourceId == null ? null : sourceRepository.findById(sourceId).map(source -> source.getCode()).orElse(null)
        );
    }
}
