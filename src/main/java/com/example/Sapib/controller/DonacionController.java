package com.example.Sapib.controller;

import com.example.Sapib.model.Usuario;
import com.example.Sapib.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class DonacionController {

    @Autowired
    private UsuarioService usuarioService;

    // Usamos Integer id para que coincida con tu UsuarioService
    @GetMapping("/voluntario/fundaciones/{id}/donar")
    @PreAuthorize("hasRole('VOLUNTARIO')") 
    public String mostrarVistaDonacion(@PathVariable("id") Integer id, Model model) {
        
        Usuario fundacion = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Fundación no encontrada"));

        model.addAttribute("fundacion", fundacion);
        
        return "voluntario/donar"; 
    }
}