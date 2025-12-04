package com.example.Sapib.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Set;

@Controller
public class HomeController {

    // 🌐 Página pública: index
    @GetMapping({"/", "/index"})
    public String mostrarInicioPublico(Authentication auth, Model model) {

        boolean logueado = (auth != null && auth.isAuthenticated());
        model.addAttribute("logueado", logueado);

        // Si está logueado, redirigir según rol
        if (logueado) {
            Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());

            if (roles.contains("ROLE_ADMIN")) {
                return "redirect:/usuarios"; // Admin va a gestión de usuarios
            } else if (roles.contains("ROLE_FUNDACION")) {
                return "redirect:/fundacion/dashboard"; 
            } else if (roles.contains("ROLE_VOLUNTARIO")) {
                return "redirect:/voluntario/dashboard";
            }
        }

        return "index"; // Mostrar index solo si NO está logueado
    }

    // 🏠 Vista home genérica (según rol)
    @GetMapping("/home")
    public String homeUsuario(Authentication auth, Model model) {

        if (auth != null && auth.isAuthenticated()) {
            Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());

            if (roles.contains("ROLE_ADMIN")) {
                model.addAttribute("rol", "ADMIN");
                return "redirect:/usuarios";
            } else if (roles.contains("ROLE_FUNDACION")) {
                model.addAttribute("rol", "FUNDACION");
            } else if (roles.contains("ROLE_VOLUNTARIO")) {
                model.addAttribute("rol", "VOLUNTARIO");
            }

            model.addAttribute("userName", auth.getName());
            return "home";
        }

        return "redirect:/login";
    }

    // 🟦 NUEVO: Vista para /admin-home
    @GetMapping("/admin-home")
    public String adminHome(Authentication auth, Model model) {

        if (auth != null) {
            Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());

            // Solo ADMIN puede entrar
            if (!roles.contains("ROLE_ADMIN")) {
                return "redirect:/home"; // Evita accesos no autorizados
            }
        }

        return "admin-home"; // Archivo admin-home.html
    }

    // ℹ️ Página "Conócenos"
    @GetMapping("/conocenos")
    public String conocenos() {
        return "conocenos";
    }

}