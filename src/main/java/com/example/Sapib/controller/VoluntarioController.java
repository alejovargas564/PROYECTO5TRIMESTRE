package com.example.Sapib.controller;

import com.example.Sapib.model.AgendarVisitaDTO;
import com.example.Sapib.model.Usuario;
import com.example.Sapib.model.Visita;
import com.example.Sapib.model.Necesidad; // Importado
import com.example.Sapib.service.UsuarioService;
import com.example.Sapib.service.VisitaService;
import com.example.Sapib.service.NecesidadService; // Importado
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;

@Controller
public class VoluntarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private VisitaService visitaService;
    
    @Autowired // NUEVO: Inyección del servicio de Necesidad
    private NecesidadService necesidadService;

    // Redirección del login: redirige directamente al listado de fundaciones
    @GetMapping("/voluntario/dashboard")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public String dashboardVoluntario() {
        return "redirect:/voluntario/fundaciones";
    }

    // ==========================================================
    // 1. LISTADO DE FUNDACIONES (El dashboard del voluntario)
    // ==========================================================
    @GetMapping("/voluntario/fundaciones")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public String listarFundaciones(Model model) {
        List<Usuario> fundaciones = usuarioService.listarFundaciones();
        model.addAttribute("fundaciones", fundaciones);
        return "voluntario/fundaciones"; // Se crea una nueva vista para esto
    }

    // ==========================================================
    // 2. AGENDAR VISITA (FORMULARIO)
    // ==========================================================
    @GetMapping("/voluntario/agendar/{idFundacion}")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public String mostrarFormularioVisita(@PathVariable Integer idFundacion, Model model) {

        Usuario fundacion = usuarioService.buscarPorId(idFundacion)
                .orElseThrow(() -> new RuntimeException("Fundación no encontrada"));

        model.addAttribute("fundacion", fundacion);
        
        AgendarVisitaDTO visitaForm = new AgendarVisitaDTO();
        visitaForm.setIdFundacion(idFundacion); // Asegurar que el ID se pase al DTO
        model.addAttribute("visitaForm", visitaForm);

        return "voluntario/agendar-visita";
    }

    // ==========================================================
    // 3. GUARDAR VISITA
    // ==========================================================
    @PostMapping("/voluntario/agendar/guardar")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public String guardarVisita(@ModelAttribute("visitaForm") AgendarVisitaDTO form, Authentication auth) {

        String userName = auth.getName();
        Usuario voluntario = usuarioService.buscarPorUserName(userName)
                .orElseThrow(() -> new RuntimeException("Voluntario actual no encontrado"));

        visitaService.agendarVisita(
                voluntario.getId(),
                form.getIdFundacion(),
                form.getFechaVisita(),
                form.getDescripcion()
        );

        return "redirect:/voluntario/historial?agendada"; // Redirige al historial
    }

    // ==========================================================
    // 4. HISTORIAL DE VISITAS (VOLUNTARIO) - CON FILTRO MULTICRITERIO
    // ==========================================================
    @GetMapping("/voluntario/historial")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public String historialVoluntario(
            // --- NUEVOS PARÁMETROS DE FILTRO ---
            @RequestParam(required = false) String fundacion,
            @RequestParam(required = false) String estado,
            // ------------------------------------
            Model model, 
            Authentication auth) {
        
        String userName = auth.getName();
        Usuario voluntario = usuarioService.buscarPorUserName(userName)
                .orElseThrow(() -> new RuntimeException("Voluntario actual no encontrado"));

        // Llama al NUEVO método del servicio que aplica los filtros
        List<Visita> visitas = visitaService.filtrarHistorialVoluntario(
                voluntario.getId(), fundacion, estado);

        model.addAttribute("visitas", visitas);
        
        // Se pasan los parámetros de filtro al modelo para mantenerlos en el formulario
        model.addAttribute("fundacion", fundacion);
        model.addAttribute("estado", estado);

        return "voluntario/historial";
    }

    // ==========================================================
    // 5. VER NECESIDADES PUBLICADAS POR UNA FUNDACION ESPECÍFICA
    // ==========================================================
    @GetMapping("/voluntario/fundaciones/{idFundacion}/necesidades")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public String listarNecesidadesPorFundacion(@PathVariable Integer idFundacion, Model model) {
        
        Usuario fundacion = usuarioService.buscarPorId(idFundacion)
                .orElseThrow(() -> new RuntimeException("Fundación no encontrada"));
        
        // Obtenemos solo las necesidades activas para esta fundación
        List<Necesidad> necesidades = necesidadService.listarPorFundacion(idFundacion); 
        
        model.addAttribute("fundacion", fundacion);
        model.addAttribute("necesidades", necesidades);
        return "voluntario/necesidades-fundacion"; // Vista de lista filtrada
    }

    // 6. VER TODAS LAS NECESIDADES ACTIVAS (Ruta genérica)
    @GetMapping("/voluntario/necesidades")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public String listarNecesidadesActivas(Model model) {
        List<Necesidad> necesidades = necesidadService.listarActivas();
        model.addAttribute("necesidades", necesidades);
        return "voluntario/necesidades"; // Nueva vista que creamos
    }
    
    @GetMapping("/voluntario/oportunidades")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public String verOportunidades() {
        return "voluntario/oportunidades"; 
    }
}