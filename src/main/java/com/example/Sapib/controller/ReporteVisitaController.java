package com.example.Sapib.controller;

import com.example.Sapib.model.Usuario;
import com.example.Sapib.model.Visita;
import com.example.Sapib.service.UsuarioService;
import com.example.Sapib.service.VisitaService;
import com.example.Sapib.repository.UsuarioRepository; // <-- IMPORTANTE: Agregar esta importación
import com.example.Sapib.utils.PdfGenerator;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
public class ReporteVisitaController {

    @Autowired
    private VisitaService visitaService;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository; // <-- AGREGADO: Inyectar el repositorio

    @Autowired
    private PdfGenerator pdfGenerator;

    // MÉTODO AUXILIAR: Búsqueda flexible para identificar al usuario por Correo, Documento o Username
    private Usuario obtenerUsuarioLogueado(Authentication auth) {
        String loginInfo = auth.getName();
        return usuarioRepository.findByCorreoUsuarioOrNumeroDocumento(loginInfo, loginInfo)
                .or(() -> usuarioRepository.findByUserName(loginInfo))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // MÉTODO MODIFICADO: Ahora usa la búsqueda flexible centralizada
    private List<Visita> getVisitasByRole(Authentication auth, String fundacion, String estado) {
        // CORREGIDO: Usamos el nuevo buscador en lugar de solo buscar por UserName
        Usuario usuario = obtenerUsuarioLogueado(auth);

        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return visitaService.listarTodasVisitas();
        } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_FUNDACION"))) {
            return visitaService.listarHistorialFundacion(usuario.getId());
        } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_VOLUNTARIO"))) {
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
            @RequestParam(required = false) String fundacion,
            @RequestParam(required = false) String estado) 
            throws IOException {

        // Se llama al método con los nuevos parámetros de búsqueda flexible
        List<Visita> visitas = getVisitasByRole(auth, fundacion, estado);
        
        // Obtiene el rol de forma segura
        String rol = auth.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .orElse("USUARIO");

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