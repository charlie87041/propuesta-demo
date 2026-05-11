package com.cookiesstore.infra.catalog.web;

import com.cookiesstore.infra.catalog.service.ProjectEnvironmentService;
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
public class ProjectEnvironmentViewController {

    private final ProjectService projectService;
    private final ProjectEnvironmentService environmentService;

    public ProjectEnvironmentViewController(ProjectService projectService, ProjectEnvironmentService environmentService) {
        this.projectService = projectService;
        this.environmentService = environmentService;
    }

    @GetMapping("/project-environments")
    public String projectIndex() {
        return "redirect:/projects";
    }

    @GetMapping("/projects/{projectId}/environments")
    public String projectEnvironments(@PathVariable String projectId, Model model) {
        prepareProjectEnvironmentModel(projectId, model, new ProjectEnvironmentForm());
        return "catalog/project-environments/manage";
    }

    @PostMapping("/projects/{projectId}/environments")
    public String store(
        @PathVariable String projectId,
        @Valid @ModelAttribute("form") ProjectEnvironmentForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareProjectEnvironmentModel(projectId, model, form);
            return "catalog/project-environments/manage";
        }

        try {
            environmentService.create(new ProjectEnvironmentService.CreateEnvironmentCommand(
                projectId,
                form.getName(),
                form.getRegion(),
                form.getDomain()
            ));
        } catch (IllegalArgumentException exception) {
            model.addAttribute("formError", exception.getMessage());
            prepareProjectEnvironmentModel(projectId, model, form);
            return "catalog/project-environments/manage";
        }

        redirectAttributes.addFlashAttribute("success", "Environment created");
        return "redirect:/projects/" + projectId + "/environments";
    }

    @GetMapping("/projects/{projectId}/environments/{environmentId}/edit")
    public String edit(@PathVariable String projectId, @PathVariable String environmentId, Model model) {
        var project = projectService.requireById(projectId);
        if (!projectService.isMutable(project)) {
            return "redirect:/projects/" + projectId + "/environments";
        }
        var environment = environmentService.requireByProjectId(projectId, environmentId);
        model.addAttribute("project", project);
        model.addAttribute("environment", environment);
        model.addAttribute("form", ProjectEnvironmentForm.from(environment));
        return "catalog/project-environments/edit";
    }

    @PostMapping("/projects/{projectId}/environments/{environmentId}")
    public String update(
        @PathVariable String projectId,
        @PathVariable String environmentId,
        @Valid @ModelAttribute("form") ProjectEnvironmentForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        var project = projectService.requireById(projectId);
        var environment = environmentService.requireByProjectId(projectId, environmentId);
        if (bindingResult.hasErrors()) {
            model.addAttribute("project", project);
            model.addAttribute("environment", environment);
            return "catalog/project-environments/edit";
        }

        try {
            environmentService.update(projectId, environmentId, new ProjectEnvironmentService.UpdateEnvironmentCommand(
                form.getName(),
                form.getRegion(),
                form.getDomain()
            ));
        } catch (IllegalArgumentException exception) {
            model.addAttribute("project", project);
            model.addAttribute("environment", environment);
            model.addAttribute("formError", exception.getMessage());
            return "catalog/project-environments/edit";
        }

        redirectAttributes.addFlashAttribute("success", "Environment updated");
        return "redirect:/projects/" + projectId + "/environments";
    }

    @PostMapping("/projects/{projectId}/environments/{environmentId}/archive")
    public String archive(
        @PathVariable String projectId,
        @PathVariable String environmentId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            environmentService.archive(projectId, environmentId);
            redirectAttributes.addFlashAttribute("success", "Environment disabled");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/projects/" + projectId + "/environments";
    }

    @PostMapping("/projects/{projectId}/environments/{environmentId}/activate")
    public String activate(
        @PathVariable String projectId,
        @PathVariable String environmentId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            environmentService.activate(projectId, environmentId);
            redirectAttributes.addFlashAttribute("success", "Environment enabled");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/projects/" + projectId + "/environments";
    }

    @PostMapping("/projects/{projectId}/environments/{environmentId}/delete")
    public String delete(
        @PathVariable String projectId,
        @PathVariable String environmentId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            environmentService.deleteIfUnused(projectId, environmentId);
            redirectAttributes.addFlashAttribute("success", "Environment deleted");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/projects/" + projectId + "/environments";
    }

    private void prepareProjectEnvironmentModel(String projectId, Model model, ProjectEnvironmentForm form) {
        var project = projectService.requireById(projectId);
        model.addAttribute("project", project);
        model.addAttribute("projectMutable", projectService.isMutable(project));
        model.addAttribute("environments", environmentService.listItemsByProject(projectId));
        model.addAttribute("form", form);
    }
}
