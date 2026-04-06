package com.example.Sapib.controller;

import com.example.Sapib.model.AgendarVisitaDTO;
import com.example.Sapib.model.Usuario;
import com.example.Sapib.model.Visita;
import com.example.Sapib.model.Necesidad;
import com.example.Sapib.service.UsuarioService;
import com.example.Sapib.service.VisitaService;
import com.example.Sapib.service.NecesidadService;
import com.example.Sapib.repository.UsuarioRepository; // <-- IMPORTANTE
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
    private UsuarioRepository usuarioRepository; // <-- AGREGADO para búsqueda flexible

    @Autowired
    private VisitaService visitaService;
    
    @Autowired 
    private NecesidadService necesidadService;

    // MÉTODO AUXILIAR: Para identificar al voluntario por Correo, Documento o Username
    private Usuario obtenerVoluntarioLogueado(Authentication auth) {
        String loginInfo = auth.getName();
        return usuarioRepository.findByCorreoUsuarioOrNumeroDocumento(loginInfo, loginInfo)
                .or(() -> usuarioRepository.findByUserName(loginInfo))
                .orElseThrow(() -> new RuntimeException("Voluntario actual no encontrado"));
    }

    @GetMapping("/voluntario/dashboard")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public String dashboardVoluntario() {
        return "redirect:/voluntario/fundaciones";
    }

    @GetMapping("/voluntario/fundaciones")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public String listarFundaciones(Model model) {
        List<Usuario> fundaciones = usuarioService.listarFundaciones();
        model.addAttribute("fundaciones", fundaciones);
        return "voluntario/fundaciones";
    }

    @GetMapping("/voluntario/agendar/{idFundacion}")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public String mostrarFormularioVisita(@PathVariable Integer idFundacion, Model model) {
        Usuario fundacion = usuarioService.buscarPorId(idFundacion)
                .orElseThrow(() -> new RuntimeException("Fundación no encontrada"));

        model.addAttribute("fundacion", fundacion);
        
        AgendarVisitaDTO visitaForm = new AgendarVisitaDTO();
        visitaForm.setIdFundacion(idFundacion);
        model.addAttribute("visitaForm", visitaForm);

        return "voluntario/agendar-visita";
    }

    @PostMapping("/voluntario/agendar/guardar")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public String guardarVisita(@ModelAttribute("visitaForm") AgendarVisitaDTO form, Authentication auth) {
        // CORREGIDO: Búsqueda flexible
        Usuario voluntario = obtenerVoluntarioLogueado(auth);

        visitaService.agendarVisita(
                voluntario.getId(),
                form.getIdFundacion(),
                form.getFechaVisita(),
                form.getDescripcion()
        );

        return "redirect:/voluntario/historial?agendada";
    }

    @GetMapping("/voluntario/historial")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public String historialVoluntario(
            @RequestParam(required = false) String fundacion,
            @RequestParam(required = false) String estado,
            Model model, 
            Authentication auth) {
        
        // CORREGIDO: Búsqueda flexible
        Usuario voluntario = obtenerVoluntarioLogueado(auth);

        List<Visita> visitas = visitaService.filtrarHistorialVoluntario(
                voluntario.getId(), fundacion, estado);

        model.addAttribute("visitas", visitas);
        model.addAttribute("fundacionNombreFiltro", fundacion); // Nombre ajustado para no confundir con el objeto usuario
        model.addAttribute("estado", estado);

        return "voluntario/historial";
    }

    @GetMapping("/voluntario/fundaciones/{idFundacion}/necesidades")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public String listarNecesidadesPorFundacion(@PathVariable Integer idFundacion, Model model) {
        Usuario fundacion = usuarioService.buscarPorId(idFundacion)
                .orElseThrow(() -> new RuntimeException("Fundación no encontrada"));
        
        List<Necesidad> necesidades = necesidadService.listarPorFundacion(idFundacion); 
        
        model.addAttribute("fundacion", fundacion);
        model.addAttribute("necesidades", necesidades);
        return "voluntario/necesidades-fundacion";
    }

    @GetMapping("/voluntario/necesidades")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public String listarNecesidadesActivas(Model model) {
        List<Necesidad> necesidades = necesidadService.listarActivas();
        model.addAttribute("necesidades", necesidades);
        return "voluntario/necesidades";
    }
    
    @GetMapping("/voluntario/oportunidades")
    @PreAuthorize("hasRole('VOLUNTARIO')")
    public String verOportunidades() {
        return "voluntario/oportunidades"; 
    }
}