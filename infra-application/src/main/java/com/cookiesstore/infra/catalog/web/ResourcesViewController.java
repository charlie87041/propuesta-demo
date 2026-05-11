package com.cookiesstore.infra.catalog.web;

import com.cookiesstore.infra.catalog.domain.ProjectEnvironment;
import com.cookiesstore.infra.catalog.service.ProjectEnvironmentService;
import com.cookiesstore.infra.topology.domain.ProjectResourceDefinition;
import com.cookiesstore.infra.topology.domain.ResourceType;
import com.cookiesstore.infra.topology.service.ProjectResourceDefinitionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
@RequestMapping("/resources")
public class ResourcesViewController {

    private final ProjectResourceDefinitionService resourceService;
    private final ProjectEnvironmentService environmentService;
    private final ObjectMapper objectMapper;

    public ResourcesViewController(
        ProjectResourceDefinitionService resourceService,
        ProjectEnvironmentService environmentService,
        ObjectMapper objectMapper
    ) {
        this.resourceService = resourceService;
        this.environmentService = environmentService;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String index(Model model) {
        model.addAttribute("resources", resourceService.listAllItems());
        return "catalog/resources/index";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("form", new GlobalVpcResourceForm());
        model.addAttribute("environments", environmentService.listActiveEnvironments());
        return "catalog/resources/new";
    }

    @PostMapping
    public String store(
        @Valid @ModelAttribute("form") GlobalVpcResourceForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("environments", environmentService.listActiveEnvironments());
            return "catalog/resources/new";
        }

        ProjectEnvironment environment = environmentService.requireById(form.getEnvironmentId());

        try {
            resourceService.create(new ProjectResourceDefinitionService.CreateProjectResourceDefinitionCommand(
                environment.getProject().getId(),
                form.getEnvironmentId(),
                ResourceType.VPC,
                form.getName(),
                form.getDescription(),
                toVpcConfigJson(form)
            ));
        } catch (IllegalArgumentException e) {
            model.addAttribute("formError", e.getMessage());
            model.addAttribute("environments", environmentService.listActiveEnvironments());
            return "catalog/resources/new";
        }

        redirectAttributes.addFlashAttribute("success", "Resource created");
        return "redirect:/resources";
    }

    @GetMapping("/{resourceId}/edit")
    public String edit(@PathVariable String resourceId, Model model) {
        ProjectResourceDefinition definition = resourceService.requireById(resourceId);
        VpcConfig config = parseVpcConfig(definition.getConfigJson());
        model.addAttribute("resource", definition);
        model.addAttribute("form", EditVpcResourceForm.from(definition, config));
        return "catalog/resources/form";
    }

    @PostMapping("/{resourceId}")
    public String update(
        @PathVariable String resourceId,
        @Valid @ModelAttribute("form") EditVpcResourceForm form,
        BindingResult bindingResult,
        Model model,
        RedirectAttributes redirectAttributes
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("resource", resourceService.requireById(resourceId));
            return "catalog/resources/form";
        }

        try {
            resourceService.update(resourceId, new ProjectResourceDefinitionService.UpdateProjectResourceDefinitionCommand(
                form.getName(),
                form.getDescription(),
                toVpcConfigJson(form)
            ));
        } catch (IllegalArgumentException e) {
            model.addAttribute("resource", resourceService.requireById(resourceId));
            model.addAttribute("formError", e.getMessage());
            return "catalog/resources/form";
        }

        redirectAttributes.addFlashAttribute("success", "Resource updated");
        return "redirect:/resources";
    }

    @PostMapping("/{resourceId}/archive")
    public String archive(@PathVariable String resourceId, RedirectAttributes redirectAttributes) {
        resourceService.archive(resourceId);
        redirectAttributes.addFlashAttribute("success", "Resource disabled");
        return "redirect:/resources";
    }

    @PostMapping("/{resourceId}/activate")
    public String activate(@PathVariable String resourceId, RedirectAttributes redirectAttributes) {
        resourceService.activate(resourceId);
        redirectAttributes.addFlashAttribute("success", "Resource enabled");
        return "redirect:/resources";
    }

    @PostMapping("/{resourceId}/delete")
    public String delete(@PathVariable String resourceId, RedirectAttributes redirectAttributes) {
        try {
            resourceService.deleteIfUnused(resourceId);
            redirectAttributes.addFlashAttribute("success", "Resource deleted");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/resources";
    }

    private String toVpcConfigJson(GlobalVpcResourceForm form) {
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
