package com.example.Sapib.controller;

import com.example.Sapib.model.Usuario;
import com.example.Sapib.model.Visita;
import com.example.Sapib.model.Necesidad;
import com.example.Sapib.service.UsuarioService;
import com.example.Sapib.service.VisitaService;
import com.example.Sapib.service.NecesidadService;
import com.example.Sapib.service.AnalyticsService;
import com.example.Sapib.repository.UsuarioRepository;
import com.example.Sapib.repository.VisitaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class FundacionController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private VisitaService visitaService;

    @Autowired
    private NecesidadService necesidadService;

    @Autowired
    private AnalyticsService analyticsService;

    @Autowired
    private VisitaRepository visitaRepository;

    private Usuario obtenerUsuarioLogueado(Authentication auth) {
        String loginInfo = auth.getName();
        return usuarioRepository.findByCorreoUsuarioOrNumeroDocumento(loginInfo, loginInfo)
                .or(() -> usuarioRepository.findByUserName(loginInfo))
                .orElseThrow(() -> new RuntimeException("Fundación no encontrada"));
    }

    // ===================== REPORTES =====================
    @GetMapping("/fundacion/reportes")
    @PreAuthorize("hasRole('FUNDACION')")
    public String verReportesImpacto(Model model, Authentication auth) {

        Usuario fundacion = obtenerUsuarioLogueado(auth);

        Map<String, Object> datos = analyticsService.prepararDatosFundacion(fundacion.getId());
        RestTemplate restTemplate = new RestTemplate();
        String url = "http://localhost:5000/api/reports/analizar-fundacion";

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, datos, Map.class);
            if (response.getBody() != null) {
                model.addAttribute("analisis", response.getBody().get("analisis"));
            }
        } catch (Exception e) {
            model.addAttribute("errorAnalisis", "Error en el motor de análisis");
        }

        model.addAttribute("visitas",
                visitaRepository.findByFundacion_IdOrderByFechaVisitaDesc(fundacion.getId()));
        model.addAttribute("fundacion", fundacion);

        return "fundacion/reportes";
    }

    // ===================== DASHBOARD =====================
    @GetMapping("/fundacion/dashboard")
    @PreAuthorize("hasRole('FUNDACION')")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("userName", auth.getName());
        return "fundacion/dashboard";
    }

    // ===================== NECESIDADES =====================
    @GetMapping("/fundacion/necesidades")
    @PreAuthorize("hasRole('FUNDACION')")
    public String gestionarNecesidades(
            @RequestParam(required = false) String titulo,
            @RequestParam(required = false) String tipo,
            Model model,
            Authentication auth) {

        Usuario fundacion = obtenerUsuarioLogueado(auth);

        List<Necesidad> necesidades = necesidadService
                .filtrarNecesidadesFundacion(fundacion.getId(), titulo, tipo);

        model.addAttribute("necesidades", necesidades);
        model.addAttribute("fundacionId", fundacion.getId());
        model.addAttribute("titulo", titulo);
        model.addAttribute("tipo", tipo);

        if (!model.containsAttribute("necesidad")) {
            model.addAttribute("necesidad", new Necesidad());
        }

        return "fundacion/necesidades";
    }

    @PostMapping("/fundacion/necesidades/guardar")
    @PreAuthorize("hasRole('FUNDACION')")
    public String guardarNecesidad(@ModelAttribute Necesidad necesidad, Authentication auth) {

        Usuario fundacion = obtenerUsuarioLogueado(auth);
        necesidadService.guardarNecesidad(necesidad, fundacion.getId());

        return "redirect:/fundacion/necesidades?guardado";
    }

    @GetMapping("/fundacion/necesidades/eliminar/{id}")
    @PreAuthorize("hasRole('FUNDACION')")
    public String eliminarNecesidad(@PathVariable Integer id) {

        necesidadService.eliminarNecesidad(id);
        return "redirect:/fundacion/necesidades?eliminado";
    }

    @GetMapping("/fundacion/necesidades/editar/{id}")
    @PreAuthorize("hasRole('FUNDACION')")
    public String editarNecesidad(
            @PathVariable Integer id,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        Necesidad necesidad = necesidadService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Necesidad no encontrada"));

        if (!necesidad.getFundacion().getUserName().equals(auth.getName())) {
            throw new RuntimeException("Acceso denegado");
        }

        redirectAttributes.addFlashAttribute("necesidad", necesidad);

        return "redirect:/fundacion/necesidades";
    }

    // ===================== VISITAS =====================
    @GetMapping("/fundacion/visitas-agendadas")
    @PreAuthorize("hasRole('FUNDACION')")
    public String visitasPendientes(Model model, Authentication auth) {

        Usuario fundacion = obtenerUsuarioLogueado(auth);
        model.addAttribute("visitas",
                visitaService.listarVisitasPendientesFundacion(fundacion.getId()));

        return "fundacion/visitas-agendadas";
    }

    @GetMapping("/fundacion/visitas/aceptar/{id}")
    @PreAuthorize("hasRole('FUNDACION')")
    public String aceptarVisita(@PathVariable Integer id) {

        visitaService.actualizarEstadoVisita(id, "ACEPTADA");
        return "redirect:/fundacion/visitas-agendadas?ok";
    }

    @GetMapping("/fundacion/visitas/rechazar/{id}")
    @PreAuthorize("hasRole('FUNDACION')")
    public String rechazarVisita(@PathVariable Integer id) {

        visitaService.actualizarEstadoVisita(id, "RECHAZADA");
        return "redirect:/fundacion/visitas-agendadas?ok";
    }

    // ===================== HISTORIAL =====================
    @GetMapping("/fundacion/historial-visitas")
    @PreAuthorize("hasRole('FUNDACION')")
    public String historial(Model model, Authentication auth) {

        Usuario fundacion = obtenerUsuarioLogueado(auth);

        List<Visita> historial = visitaService
                .listarHistorialFundacion(fundacion.getId());

        model.addAttribute("visitas", historial);
        model.addAttribute("fundacion", fundacion);

        return "fundacion/historial-visitas";
    }

    // ===================== EVENTOS =====================
    @GetMapping("/fundacion/eventos")
    @PreAuthorize("hasRole('FUNDACION')")
    public String eventos() {
        return "fundacion/eventos";
    }
}

