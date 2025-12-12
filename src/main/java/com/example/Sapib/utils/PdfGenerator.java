package com.example.Sapib.utils;

import com.example.Sapib.model.Visita; // <--- IMPORTACIÓN AÑADIDA
import freemarker.template.Template;
import org.springframework.stereotype.Component;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import jakarta.servlet.http.HttpServletResponse;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

@Component
public class PdfGenerator {

    private final FreeMarkerConfigurer configurer;

    public PdfGenerator(FreeMarkerConfigurer configurer) {
        this.configurer = configurer;
    }

    public void generarPdf(
            String templateName,
            List<?> datos,
            LocalDateTime desde,
            LocalDateTime hasta,
            HttpServletResponse response) throws Exception {

        // Convertir LocalDateTime a LocalDate (si solo usas fecha)
        LocalDate fechaDesde = (desde != null) ? desde.toLocalDate() : null;
        LocalDate fechaHasta = (hasta != null) ? hasta.toLocalDate() : null;

        Map<String, Object> model = new HashMap<>();
        model.put("usuarios", datos);
        model.put("desde", fechaDesde);
        model.put("hasta", fechaHasta);

        Template template = configurer.getConfiguration().getTemplate(templateName + ".html");
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reporte-usuarios.pdf");

        OutputStream out = response.getOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(out);
        out.close();
    }
    
    // Método para generar el reporte de Visitas
    public void generarPdfVisitas(
            String rol,
            List<Visita> visitas,
            HttpServletResponse response) throws Exception {

        Map<String, Object> model = new HashMap<>();
        model.put("visitas", visitas);
        model.put("rol", rol); // Para personalizar el título del reporte

        Template template = configurer.getConfiguration().getTemplate("reporte-visitas.html");
        String html = FreeMarkerTemplateUtils.processTemplateIntoString(template, model);

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=reporte-visitas-" + rol.toLowerCase() + ".pdf");

        OutputStream out = response.getOutputStream();
        ITextRenderer renderer = new ITextRenderer();
        renderer.setDocumentFromString(html);
        renderer.layout();
        renderer.createPDF(out);
        out.close();
    }
}