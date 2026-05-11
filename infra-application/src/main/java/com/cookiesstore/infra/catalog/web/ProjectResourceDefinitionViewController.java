package com.cookiesstore.infra.catalog.web;

import com.cookiesstore.infra.catalog.domain.Project;
import com.cookiesstore.infra.catalog.domain.ProjectEnvironment;
import com.cookiesstore.infra.catalog.service.ProjectEnvironmentService;
import com.cookiesstore.infra.catalog.service.ProjectService;
import com.cookiesstore.infra.topology.domain.ProjectResourceDefinition;
import com.cookiesstore.infra.topology.domain.ResourceType;
import com.cookiesstore.infra.topology.service.ProjectResourceDefinitionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import software.amazon.awssdk.services.ec2.model.Vpc;

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
@RequestMapping("/projects/{projectId}/environments/{environmentId}/resources")
public class ProjectResourceDefinitionViewController {

    private final ProjectService projectService;
    private final ProjectEnvironmentService environmentService;
    private final ProjectResourceDefinitionService resourceService;
    private final ObjectMapper objectMapper;

    public ProjectResourceDefinitionViewController(
        ProjectService projectService,
        ProjectEnvironmentService environmentService,
        ProjectResourceDefinitionService resourceService,
        ObjectMapper objectMapper
    ) {
        this.projectService = projectService;
        this.environmentService = environmentService;
        this.resourceService = resourceService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String index(
        @PathVariable String projectId,
        @PathVariable String environmentId,
        Model model
    ) {
        var vpc = new Vpc;
        Project project = projectService.requireById(projectId);
        ProjectEnvironment environment = environmentService.requireByProjectId(projectId, environmentId);
        prepareIndexModel(model, project, environment, new VpcResourceForm());
        return "topology/resources/index";
    }

    @PostMapping
    public String store(
        @PathVariable String projectId,
        @PathVariable String environmentId,
        @Valid @ModelAttribute("form") VpcResourceForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        Project project = projectService.requireById(projectId);
        ProjectEnvironment environment = environmentService.requireByProjectId(projectId, environmentId);

        if (bindingResult.hasErrors()) {
            prepareIndexModel(model, project, environment, form);
            return "topology/resources/index";
        }

        try {
            resourceService.create(new ProjectResourceDefinitionService.CreateProjectResourceDefinitionCommand(
                projectId,
                environmentId,
                ResourceType.VPC,
                form.getName(),
                form.getDescription(),
                toVpcConfigJson(form)
            ));
        } catch (IllegalArgumentException e) {
            model.addAttribute("formError", e.getMessage());
            prepareIndexModel(model, project, environment, form);
            return "topology/resources/index";
        }

        redirectAttributes.addFlashAttribute("success", "Resource created");
        return "redirect:/projects/" + projectId + "/environments/" + environmentId + "/resources";
    }

    @GetMapping("/{resourceId}/edit")
    public String edit(
        @PathVariable String projectId,
        @PathVariable String environmentId,
        @PathVariable String resourceId,
        Model model
    ) {
        Project project = projectService.requireById(projectId);
        ProjectEnvironment environment = environmentService.requireByProjectId(projectId, environmentId);
        ProjectResourceDefinition definition = resourceService.requireById(resourceId);
        VpcConfig config = parseVpcConfig(definition.getConfigJson());
        model.addAttribute("project", project);
        model.addAttribute("environment", environment);
        model.addAttribute("resource", definition);
        model.addAttribute("form", EditVpcResourceForm.from(definition, config));
        return "topology/resources/form";
    }

    @PostMapping("/{resourceId}")
    public String update(
        @PathVariable String projectId,
        @PathVariable String environmentId,
        @PathVariable String resourceId,
        @Valid @ModelAttribute("form") EditVpcResourceForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            Project project = projectService.requireById(projectId);
            ProjectEnvironment environment = environmentService.requireByProjectId(projectId, environmentId);
            ProjectResourceDefinition definition = resourceService.requireById(resourceId);
            model.addAttribute("project", project);
            model.addAttribute("environment", environment);
            model.addAttribute("resource", definition);
            return "topology/resources/form";
        }

        try {
            resourceService.update(resourceId, new ProjectResourceDefinitionService.UpdateProjectResourceDefinitionCommand(
                form.getName(),
                form.getDescription(),
                toVpcConfigJson(form)
            ));
        } catch (IllegalArgumentException e) {
            Project project = projectService.requireById(projectId);
            ProjectEnvironment environment = environmentService.requireByProjectId(projectId, environmentId);
            ProjectResourceDefinition definition = resourceService.requireById(resourceId);
            model.addAttribute("project", project);
            model.addAttribute("environment", environment);
            model.addAttribute("resource", definition);
            model.addAttribute("formError", e.getMessage());
            return "topology/resources/form";
        }

        redirectAttributes.addFlashAttribute("success", "Resource updated");
        return "redirect:/projects/" + projectId + "/environments/" + environmentId + "/resources";
    }

    @PostMapping("/{resourceId}/archive")
    public String archive(
        @PathVariable String projectId,
        @PathVariable String environmentId,
        @PathVariable String resourceId,
        RedirectAttributes redirectAttributes
    ) {
        resourceService.archive(resourceId);
        redirectAttributes.addFlashAttribute("success", "Resource disabled");
        return "redirect:/projects/" + projectId + "/environments/" + environmentId + "/resources";
    }

    @PostMapping("/{resourceId}/activate")
    public String activate(
        @PathVariable String projectId,
        @PathVariable String environmentId,
        @PathVariable String resourceId,
        RedirectAttributes redirectAttributes
    ) {
        resourceService.activate(resourceId);
        redirectAttributes.addFlashAttribute("success", "Resource enabled");
        return "redirect:/projects/" + projectId + "/environments/" + environmentId + "/resources";
    }

    @PostMapping("/{resourceId}/delete")
    public String delete(
        @PathVariable String projectId,
        @PathVariable String environmentId,
        @PathVariable String resourceId,
        RedirectAttributes redirectAttributes
    ) {
        try {
            resourceService.deleteIfUnused(resourceId);
            redirectAttributes.addFlashAttribute("success", "Resource deleted");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/projects/" + projectId + "/environments/" + environmentId + "/resources";
    }

    private void prepareIndexModel(Model model, Project project, ProjectEnvironment environment, VpcResourceForm form) {
        model.addAttribute("project", project);
        model.addAttribute("environment", environment);
        model.addAttribute("resources", resourceService.listItemsByEnvironment(environment.getId()));
        model.addAttribute("form", form);
        model.addAttribute("projectMutable", projectService.isMutable(project));
    }

    private String toVpcConfigJson(VpcResourceForm form) {
        try {
            return objectMapper.writeValueAsString(new VpcConfig(
                form.getCidrBlock(),
                form.isEnableDnsSupport(),
                form.isEnableDnsHostnames()
            ));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private String toVpcConfigJson(EditVpcResourceForm form) {
        try {
            return objectMapper.writeValueAsString(new VpcConfig(
                form.getCidrBlock(),
                form.isEnableDnsSupport(),
                form.isEnableDnsHostnames()
            ));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private VpcConfig parseVpcConfig(String configJson) {
        try {
            return objectMapper.readValue(configJson, VpcConfig.class);
        } catch (JsonProcessingException e) {
            return VpcConfig.defaults();
        }
    }
}
