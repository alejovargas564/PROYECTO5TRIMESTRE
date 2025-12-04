package com.example.Sapib.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.core.Authentication;

@Controller
public class FundacionController {

    @GetMapping("/fundacion/dashboard")
    @PreAuthorize("hasRole('FUNDACION')")
    public String dashboardFundacion(Model model, Authentication auth) {
        if (auth != null) {
            model.addAttribute("userName", auth.getName());
        }
        return "fundacion/dashboard";
    }
    
    // Puedes agregar más rutas específicas para fundaciones
    @GetMapping("/fundacion/eventos")
    @PreAuthorize("hasRole('FUNDACION')")
    public String gestionarEventos() {
        return "fundacion/eventos"; // Puedes crear esta vista después
    }
}