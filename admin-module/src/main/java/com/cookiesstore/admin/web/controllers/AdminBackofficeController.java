package com.cookiesstore.admin.web.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminBackofficeController {

    @GetMapping("/admin")
    public String index() {
        return "backoffice/index";
    }
}
