package com.cookiesstore.infra.catalog.web;

import com.cookiesstore.infra.catalog.domain.ApplicationBuildType;
import com.cookiesstore.infra.catalog.domain.ApplicationServiceType;
import com.cookiesstore.infra.catalog.service.ProjectApplicationService;
import com.cookiesstore.infra.catalog.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ProjectApplicationViewController {

    private final ProjectService projectService;
    private final ProjectApplicationService applicationService;

    public ProjectApplicationViewController(ProjectService projectService, ProjectApplicationService applicationService) {
        this.projectService = projectService;
        this.applicationService = applicationService;
    }

    @GetMapping("/applications")
    public String index(Model model) {
        model.addAttribute("applications", applicationService.listItems());
        model.addAttribute("activeProjects", projectService.listActive());
        return "catalog/applications/index";
    }

    @GetMapping("/projects/{projectId}/applications")
    public String projectApplications(@PathVariable String projectId) {
        return "redirect:/applications";
    }

    @GetMapping("/projects/{projectId}/applications/new")
    public String createForProject(@PathVariable String projectId) {
        return "redirect:/applications/new";
    }

    @GetMapping("/applications/new")
    public String create(Model model) {
        prepareFormModel(model, new ProjectApplicationForm());
        return "catalog/applications/form";
    }

    @PostMapping("/applications")
    public String store(
        @Valid @ModelAttribute("form") ProjectApplicationForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareFormModel(model, form);
            return "catalog/applications/form";
        }

        try {
            applicationService.create(new ProjectApplicationService.CreateApplicationCommand(
                form.getProjectId(),
                form.getKey(),
                form.getName(),
                form.getDescription(),
                form.getBuildType(),
                form.getServiceType(),
                form.getRuntime(),
                form.getSourceLocation(),
                form.getDefaultPort()
            ));
        } catch (IllegalArgumentException exception) {
            model.addAttribute("formError", exception.getMessage());
            prepareFormModel(model, form);
            return "catalog/applications/form";
        }

        redirectAttributes.addFlashAttribute("success", "Application created");
        return "redirect:/applications";
    }

    @GetMapping("/projects/{projectId}/applications/{applicationId}/edit")
    public String editForProject(@PathVariable String projectId, @PathVariable String applicationId, Model model) {
        var application = this.applicationService.requireById(applicationId);
        model.addAttribute(applicationId, application);
        return "redirect:/applications/" + applicationId + "/edit";
    }

    @GetMapping("/applications/{applicationId}/edit")
    public String edit(@PathVariable String applicationId, Model model, RedirectAttributes redirectAttributes) {
        var application = applicationService.requireById(applicationId);
        if (!projectService.isMutable(application.getProject())) {
            redirectAttributes.addFlashAttribute("error", "Archived projects are read-only");
            return "redirect:/applications";
        }
        model.addAttribute("application", application);
        model.addAttribute("applicationId", applicationId);
        prepareFormReferenceData(model);
        model.addAttribute("form", ProjectApplicationForm.from(application));
        return "catalog/applications/edit";
    }

    @PostMapping("/applications/{applicationId}")
    public String update(
        @PathVariable String applicationId,
        @Valid @ModelAttribute("form") ProjectApplicationForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        var application = applicationService.requireById(applicationId);
        if (bindingResult.hasErrors()) {
            model.addAttribute("application", application);
            model.addAttribute("applicationId", applicationId);
            prepareFormReferenceData(model);
            return "catalog/applications/edit";
        }

        try {
            applicationService.update(application.getProject().getId(), applicationId, new ProjectApplicationService.UpdateApplicationCommand(
                form.getKey(),
                form.getName(),
                form.getDescription(),
                form.getBuildType(),
                form.getServiceType(),
                form.getRuntime(),
                form.getSourceLocation(),
                form.getDefaultPort()
            ));
        } catch (IllegalArgumentException exception) {
            model.addAttribute("application", application);
            model.addAttribute("applicationId", applicationId);
            model.addAttribute("formError", exception.getMessage());
            prepareFormReferenceData(model);
            return "catalog/applications/edit";
        }

        redirectAttributes.addFlashAttribute("success", "Application updated");
        return "redirect:/applications";
    }

    @PostMapping("/projects/{projectId}/applications/{applicationId}/archive")
    public String archiveForProject(@PathVariable String projectId, @PathVariable String applicationId) {
        return "redirect:/applications";
    }

    @PostMapping("/applications/{applicationId}/archive")
    public String archive(
        @PathVariable String applicationId,
        RedirectAttributes redirectAttributes
    ) {
        var application = applicationService.requireById(applicationId);
        try {
            applicationService.archive(application.getProject().getId(), applicationId);
            redirectAttributes.addFlashAttribute("success", "Application disabled");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/applications";
    }

    @PostMapping("/projects/{projectId}/applications/{applicationId}/activate")
    public String activateForProject(@PathVariable String projectId, @PathVariable String applicationId) {
        return "redirect:/applications";
    }

    @PostMapping("/applications/{applicationId}/activate")
    public String activate(
        @PathVariable String applicationId,
        RedirectAttributes redirectAttributes
    ) {
        var application = applicationService.requireById(applicationId);
        try {
            applicationService.activate(application.getProject().getId(), applicationId);
            redirectAttributes.addFlashAttribute("success", "Application enabled");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/applications";
    }

    @PostMapping("/projects/{projectId}/applications/{applicationId}/delete")
    public String deleteForProject(@PathVariable String projectId, @PathVariable String applicationId) {
        return "redirect:/applications";
    }

    @PostMapping("/applications/{applicationId}/delete")
    public String delete(
        @PathVariable String applicationId,
        RedirectAttributes redirectAttributes
    ) {
        var application = applicationService.requireById(applicationId);
        try {
            applicationService.deleteIfUnused(application.getProject().getId(), applicationId);
            redirectAttributes.addFlashAttribute("success", "Application deleted");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/applications";
    }

    private void prepareFormModel(Model model, ProjectApplicationForm form) {
        prepareFormReferenceData(model);
        model.addAttribute("form", form);
    }

    private void prepareFormReferenceData(Model model) {
        model.addAttribute("projects", projectService.listActive());
        model.addAttribute("buildTypes", ApplicationBuildType.values());
        model.addAttribute("serviceTypes", ApplicationServiceType.values());
    }
}
