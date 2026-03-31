package com.example.Sapib.controller;

import com.example.Sapib.model.Usuario;
import com.example.Sapib.model.Visita;
import com.example.Sapib.model.Necesidad;
import com.example.Sapib.service.UsuarioService;
import com.example.Sapib.service.VisitaService;
import com.example.Sapib.service.NecesidadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // <-- IMPORTACIÓN NECESARIA

import java.util.List;

@Controller
public class FundacionController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private VisitaService visitaService;
    
    @Autowired
    private NecesidadService necesidadService; 

    @GetMapping("/fundacion/dashboard")
    @PreAuthorize("hasRole('FUNDACION')")
    public String dashboardFundacion(Model model, Authentication auth) {
        if (auth != null) {
            model.addAttribute("userName", auth.getName());
        }
        return "fundacion/dashboard";
    }

    // ==========================================================
    // 1. VISITAS AGENDADAS PENDIENTES
    // ==========================================================
    @GetMapping("/fundacion/visitas-agendadas")
    @PreAuthorize("hasRole('FUNDACION')")
    public String listarVisitasPendientes(Model model, Authentication auth) {
        String userName = auth.getName();
        Usuario fundacion = usuarioService.buscarPorUserName(userName)
                .orElseThrow(() -> new RuntimeException("Fundación actual no encontrada"));

        List<Visita> visitas = visitaService.listarVisitasPendientesFundacion(fundacion.getId());
        model.addAttribute("visitas", visitas);
        return "fundacion/visitas-agendadas";
    }

    // ==========================================================
    // 2. ACEPTAR/RECHAZAR VISITA
    // ==========================================================
    @GetMapping("/fundacion/visitas/aceptar/{idVisita}")
    @PreAuthorize("hasRole('FUNDACION')")
    public String aceptarVisita(@PathVariable Integer idVisita) {
        visitaService.actualizarEstadoVisita(idVisita, "ACEPTADA");
        return "redirect:/fundacion/visitas-agendadas?aceptada";
    }

    @GetMapping("/fundacion/visitas/rechazar/{idVisita}")
    @PreAuthorize("hasRole('FUNDACION')")
    public String rechazarVisita(@PathVariable Integer idVisita) {
        visitaService.actualizarEstadoVisita(idVisita, "RECHAZADA");
        return "redirect:/fundacion/visitas-agendadas?rechazada";
    }

    // ==========================================================
    // 3. HISTORIAL DE VISITAS (Todas las que recibió)
    // ==========================================================
    @GetMapping("/fundacion/historial-visitas")
    @PreAuthorize("hasRole('FUNDACION')")
    public String historialFundacion(Model model, Authentication auth) {
        String userName = auth.getName();
        Usuario fundacion = usuarioService.buscarPorUserName(userName)
                .orElseThrow(() -> new RuntimeException("Fundación actual no encontrada"));

        List<Visita> visitas = visitaService.listarHistorialFundacion(fundacion.getId());
        model.addAttribute("visitas", visitas);
        return "fundacion/historial-visitas";
    }

    // Se mantiene la ruta de eventos
    @GetMapping("/fundacion/eventos")
    @PreAuthorize("hasRole('FUNDACION')")
    public String gestionarEventos() {
        return "fundacion/eventos"; 
    }
    
    // ==========================================================
    // 4. GESTIÓN DE NECESIDADES (CRUD) - CON FILTRO
    // ==========================================================

    /**
     * Vista principal y listado (Maneja listado, filtros y la carga inicial/edición del formulario).
     */
    @GetMapping("/fundacion/necesidades")
    @PreAuthorize("hasRole('FUNDACION')")
    public String gestionarNecesidades(
        @RequestParam(required = false) String titulo, 
        @RequestParam(required = false) String tipo,
        Model model, 
        Authentication auth) {
        
        String userName = auth.getName();
        Usuario fundacion = usuarioService.buscarPorUserName(userName)
                .orElseThrow(() -> new RuntimeException("Fundación no encontrada"));
        
        // Listar y FILTRAR necesidades de esta fundación
        List<Necesidad> necesidades = necesidadService.filtrarNecesidadesFundacion(
            fundacion.getId(), titulo, tipo); 
        
        model.addAttribute("necesidades", necesidades);
        model.addAttribute("fundacionId", fundacion.getId());
        
        // Añadir parámetros de filtro al modelo para retenerlos en el formulario
        model.addAttribute("titulo", titulo);
        model.addAttribute("tipo", tipo);
        
        // CORRECCIÓN CLAVE: Solo inicializa una nueva Necesidad si NO está ya en el modelo
        // (el objeto 'necesidad' es colocado por 'editarNecesidad' a través de FlashAttributes)
        if (!model.containsAttribute("necesidad")) {
             model.addAttribute("necesidad", new Necesidad());
        }

        return "fundacion/necesidades";
    }

    /**
     * Carga la necesidad y **REDIRIGE** al método principal usando Flash Attributes.
     * Esto limpia la petición y fuerza a la vista a usar el objeto cargado para la edición.
     */
    @GetMapping("/fundacion/necesidades/editar/{id}")
    @PreAuthorize("hasRole('FUNDACION')")
    public String editarNecesidad(
            @PathVariable Integer id, 
            Model model, 
            Authentication auth,
            RedirectAttributes redirectAttributes) { // <-- NUEVO ARGUMENTO
        
        Necesidad necesidad = necesidadService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Necesidad no encontrada"));
        
        // Validar que la necesidad pertenezca a la fundación logueada
        if (!necesidad.getFundacion().getUserName().equals(auth.getName())) {
             throw new RuntimeException("Acceso denegado a esta necesidad");
        }

        // 🎯 CAMBIO CLAVE: Usamos Flash Attribute para enviar el objeto en la redirección
        redirectAttributes.addFlashAttribute("necesidad", necesidad);
        
        // 🎯 CAMBIO CLAVE: Hacemos una redirección, no un forward
        return "redirect:/fundacion/necesidades"; 
    }

    // Guardar (Crear o Actualizar)
    @PostMapping("/fundacion/necesidades/guardar")
    @PreAuthorize("hasRole('FUNDACION')")
    public String guardarNecesidad(@ModelAttribute Necesidad necesidad, Authentication auth) {
        String userName = auth.getName();
        Usuario fundacion = usuarioService.buscarPorUserName(userName)
                .orElseThrow(() -> new RuntimeException("Fundación no encontrada"));

        necesidadService.guardarNecesidad(necesidad, fundacion.getId());
        
        return "redirect:/fundacion/necesidades?guardado";
    }

    // Eliminar
    @GetMapping("/fundacion/necesidades/eliminar/{id}")
    @PreAuthorize("hasRole('FUNDACION')")
    public String eliminarNecesidad(@PathVariable Integer id) {
        necesidadService.eliminarNecesidad(id);
        return "redirect:/fundacion/necesidades?eliminado";
    }
}

