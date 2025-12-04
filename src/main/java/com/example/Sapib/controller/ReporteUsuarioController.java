package com.example.Sapib.controller;

import com.example.Sapib.model.Usuario;
import com.example.Sapib.service.UsuarioService;
import com.example.Sapib.utils.PdfGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
public class ReporteUsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private PdfGenerator pdfGenerator;

    // Vista con filtros
    @GetMapping("/vista-usuarios-reportes")
    public String vistaUsuarios(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime hasta,
            Model model) {

        List<Usuario> usuarios = usuarioService.filtrarUsuarios(nombre, desde, hasta);

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("nombre", nombre);
        model.addAttribute("desde", desde);
        model.addAttribute("hasta", hasta);

        return "vista-usuarios";
    }

    // Descargar PDF
    @GetMapping("/reporte-usuarios")
    public void descargarPDF(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime desde,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm") LocalDateTime hasta,
            HttpServletResponse response) throws IOException {

        List<Usuario> usuarios = usuarioService.filtrarUsuarios(nombre, desde, hasta);

        try {
            pdfGenerator.generarPdf("/reporte-usuarios", usuarios, desde, hasta, response);

        } catch (Exception ex) {
            ex.printStackTrace();
            response.reset();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.setContentType("text/plain; charset=UTF-8");
            response.getWriter().write("⚠ Error generando PDF: " + ex.getMessage());
            response.getWriter().flush();
        }
    }
}
