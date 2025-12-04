package com.example.Sapib.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminController {

    @GetMapping("/admin/home")
    public String homeAdmin() {
        return "admin-home"; // nombre de la vista
    }
}
