package com.cookiesstore.pos.web.advice.model;

import com.cookiesstore.pos.dto.CloseSessionForm;
import com.cookiesstore.pos.dto.StartSessionForm;
import com.cookiesstore.pos.repository.PosSourceRepository;
import com.cookiesstore.pos.web.PosSessionController;
import com.cookiesstore.pos.web.advice.support.BaseAdviceSupport;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@ControllerAdvice(assignableTypes = PosSessionController.class)
public class PosSessionModelAdvice extends BaseAdviceSupport {

    private final PosSourceRepository sourceRepository;

    public PosSessionModelAdvice(PosSourceRepository sourceRepository) {
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
        if (!model.containsAttribute("openForm")) {
            model.addAttribute("openForm", new StartSessionForm(null));
        }
        if (!model.containsAttribute("closeForm")) {
            model.addAttribute("closeForm", new CloseSessionForm());
        }
    }
}
