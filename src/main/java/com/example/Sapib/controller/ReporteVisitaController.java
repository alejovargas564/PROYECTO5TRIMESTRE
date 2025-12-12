package com.example.Sapib.controller;

import com.example.Sapib.model.Usuario;
import com.example.Sapib.model.Visita;
import com.example.Sapib.service.UsuarioService;
import com.example.Sapib.service.VisitaService;
import com.example.Sapib.utils.PdfGenerator;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam; // Importación necesaria

import java.io.IOException;
import java.util.List;

@Controller
public class ReporteVisitaController {

    @Autowired
    private VisitaService visitaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PdfGenerator pdfGenerator;

    // MÉTODO MODIFICADO: Ahora acepta los parámetros de filtro
    private List<Visita> getVisitasByRole(Authentication auth, String fundacion, String estado) {
        String userName = auth.getName();
        Usuario usuario = usuarioService.buscarPorUserName(userName)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return visitaService.listarTodasVisitas();
        } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_FUNDACION"))) {
            return visitaService.listarHistorialFundacion(usuario.getId());
        } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VOLUNTARIO"))) {
            // CAMBIO CLAVE: Llama al método con filtros para el voluntario
            return visitaService.filtrarHistorialVoluntario(usuario.getId(), fundacion, estado);
        }
        return List.of();
    }

    // ==========================================================
    // PDF GENÉRICO DE VISITAS - FIRMA MODIFICADA
    // ==========================================================
    @GetMapping({"/reporte-visitas-admin", "/reporte-visitas-fundacion", "/reporte-visitas-voluntario"})
    @PreAuthorize("hasAnyRole('ADMIN', 'FUNDACION', 'VOLUNTARIO')")
    public void descargarPDFVisitas(
            HttpServletResponse response, 
            Authentication auth,
            // --- NUEVOS PARÁMETROS RECIBIDOS ---
            @RequestParam(required = false) String fundacion,
            @RequestParam(required = false) String estado) 
            throws IOException {

        // Se llama al método con los nuevos parámetros
        List<Visita> visitas = getVisitasByRole(auth, fundacion, estado);
        
        // Obtiene el rol (ej: ADMIN, FUNDACION) para el nombre del archivo PDF
        String rol = auth.getAuthorities().stream().findFirst().get().getAuthority().replace("ROLE_", "");

        try {
            pdfGenerator.generarPdfVisitas(rol, visitas, response);

        } catch (Exception ex) {
            ex.printStackTrace();
            response.reset();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write("⚠ Error generando PDF de Visitas: " + ex.getMessage());
            response.getWriter().flush();
        }
    }
}