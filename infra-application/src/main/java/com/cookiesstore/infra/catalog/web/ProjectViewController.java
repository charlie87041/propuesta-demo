package com.cookiesstore.infra.catalog.web;

import com.cookiesstore.infra.catalog.service.CloudProviderService;
import com.cookiesstore.infra.catalog.service.ProjectService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/projects")
public class ProjectViewController {

    private final ProjectService projectService;
    private final CloudProviderService cloudProviderService;

    public ProjectViewController(ProjectService projectService, CloudProviderService cloudProviderService) {
        this.projectService = projectService;
        this.cloudProviderService = cloudProviderService;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("projects", projectService.listItems());
        return "catalog/projects/index";
    }

    @GetMapping("/new")
    public String create(Model model) {
        prepareForm(model, new ProjectForm());
        return "catalog/projects/form";
    }

    @PostMapping
    public String store(
        @Valid @ModelAttribute("form") ProjectForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            prepareForm(model, form);
            return "catalog/projects/form";
        }

        try {
            projectService.create(new ProjectService.CreateProjectCommand(
                form.getKey(),
                form.getName(),
                form.getDescription(),
                form.getDefaultRegion(),
                form.getOwner(),
                form.getProviderCode(),
                form.getProviderCredentialsJson()
            ));
        } catch (IllegalArgumentException exception) {
            model.addAttribute("formError", exception.getMessage());
            prepareForm(model, form);
            return "catalog/projects/form";
        }

        redirectAttributes.addFlashAttribute("success", "Project created");
        return "redirect:/projects";
    }

    @GetMapping("/{projectId}/edit")
    public String edit(@PathVariable String projectId, Model model) {
        var project = projectService.requireById(projectId);
        model.addAttribute("project", project);
        model.addAttribute("form", EditProjectForm.from(project));
        return "catalog/projects/edit";
    }

    @PostMapping("/{projectId}")
    public String update(
        @PathVariable String projectId,
        @Valid @ModelAttribute("form") EditProjectForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        var project = projectService.requireById(projectId);
        if (bindingResult.hasErrors()) {
            model.addAttribute("project", project);
            return "catalog/projects/edit";
        }

        try {
            projectService.update(projectId, new ProjectService.UpdateProjectCommand(
                form.getName(),
                form.getDescription(),
                form.getDefaultRegion(),
                form.getOwner()
            ));
        } catch (IllegalArgumentException exception) {
            model.addAttribute("project", project);
            model.addAttribute("formError", exception.getMessage());
            return "catalog/projects/edit";
        }

        redirectAttributes.addFlashAttribute("success", "Project updated");
        return "redirect:/projects";
    }

    @PostMapping("/{projectId}/archive")
    public String archive(@PathVariable String projectId, RedirectAttributes redirectAttributes) {
        projectService.archive(projectId);
        redirectAttributes.addFlashAttribute("success", "Project disabled");
        return "redirect:/projects";
    }

    @PostMapping("/{projectId}/activate")
    public String activate(@PathVariable String projectId, RedirectAttributes redirectAttributes) {
        projectService.activate(projectId);
        redirectAttributes.addFlashAttribute("success", "Project enabled");
        return "redirect:/projects";
    }

    @PostMapping("/{projectId}/delete")
    public String delete(@PathVariable String projectId, RedirectAttributes redirectAttributes) {
        try {
            projectService.deleteIfEmpty(projectId);
            redirectAttributes.addFlashAttribute("success", "Project deleted");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
        }
        return "redirect:/projects";
    }

    @PostMapping("/{projectId}/credentials")
    public String updateCredentials(
        @PathVariable String projectId,
        @ModelAttribute("providerCredentialsJson") String providerCredentialsJson,
        RedirectAttributes redirectAttributes
    ) {
        try {
            projectService.updateProviderCredentials(projectId, providerCredentialsJson);
            redirectAttributes.addFlashAttribute("success", "Provider credentials updated");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("error", exception.getMessage());
            return "redirect:/projects/" + projectId + "/edit";
        }
        return "redirect:/projects/" + projectId + "/edit";
    }

    private void prepareForm(Model model, ProjectForm form) {
        model.addAttribute("form", form);
        model.addAttribute("providers", cloudProviderService.listActive());
    }
}
