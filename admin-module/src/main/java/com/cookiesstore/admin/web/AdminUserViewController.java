package com.cookiesstore.admin.web;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AdminUserViewController {

    @GetMapping("/admin/users")
    public String usersList() {
        return "backoffice/users/index";
    }

    @GetMapping("/admin/users/new")
    public String createUser(Model model) {
        model.addAttribute("pageTitle", "Crear usuario admin");
        model.addAttribute("isEdit", false);
        return "backoffice/users/form";
    }

    @GetMapping("/admin/users/{userId}/edit")
    public String editUser(@PathVariable Long userId, Model model) {
        model.addAttribute("pageTitle", "Editar usuario admin");
        model.addAttribute("isEdit", true);
        model.addAttribute("userId", userId);
        return "backoffice/users/form";
    }
}
