package com.example.Sapib.controller;

import com.example.Sapib.model.Necesidad;
import com.example.Sapib.model.Visita;
import com.example.Sapib.service.NecesidadService;
import com.example.Sapib.service.VisitaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminController {

    @Autowired
    private VisitaService visitaService;

    @Autowired // Inyectar el nuevo servicio
    private NecesidadService necesidadService;

    @GetMapping("/admin/home")
    public String homeAdmin() {
        return "admin-home"; // nombre de la vista
    }

    // ==========================================================
    // 1. LISTADO DE TODAS LAS VISITAS (ADMIN)
    // ==========================================================
    @GetMapping("/admin/visitas")
    @PreAuthorize("hasRole('ADMIN')")
    public String listarTodasLasVisitas(Model model) {
        List<Visita> visitas = visitaService.listarTodasVisitas();
        model.addAttribute("visitas", visitas);
        return "admin/visitas";
    }
    
    // ==========================================================
    // 2. GESTIÓN DE NECESIDADES (ADMIN) - Listar
    // ==========================================================
    @GetMapping("/admin/necesidades") // Mapeo corregido
    @PreAuthorize("hasRole('ADMIN')") // Seguridad añadida
    public String listarNecesidades(Model model) {
        // 1. Obtener todas las necesidades
        List<Necesidad> necesidades = necesidadService.findAllNecesidades();
        
        // 2. Agregar a la vista
        model.addAttribute("necesidades", necesidades);
        
        // 3. Devolver la plantilla
        return "admin/necesidades"; // Busca la plantilla en /templates/admin/necesidades.html
    }

    // ==========================================================
    // 3. GESTIÓN DE NECESIDADES (ADMIN) - Eliminar/Moderar
    // ==========================================================
    /**
     * Elimina una necesidad por su ID.
     * Mapea a /admin/necesidades/eliminar/{id}
     */
    @PostMapping("/admin/necesidades/eliminar/{id}") // Mapeo corregido
    @PreAuthorize("hasRole('ADMIN')") // Seguridad añadida
    public String eliminarNecesidad(@PathVariable("id") Integer id, RedirectAttributes redirectAttributes) {
        try {
            necesidadService.deleteNecesidadById(id);
            redirectAttributes.addFlashAttribute("successMessage", 
                "✅ Necesidad eliminada exitosamente.");
        } catch (Exception e) {
            // Se usa e.getMessage() para mostrar detalles del error del servicio (ej. "La necesidad no existe")
            redirectAttributes.addFlashAttribute("errorMessage", 
                "❌ Error al eliminar la necesidad: " + e.getMessage()); 
        }
        
        // Redirigir de vuelta a la lista de necesidades
        return "redirect:/admin/necesidades"; 
    }
}