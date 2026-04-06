package com.example.Sapib.controller;

import com.example.Sapib.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import java.util.*;

@Controller
public class AnalyticsController {

    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/admin/dashboard-impacto")
    public String mostrarDashboard(Model model) {
        try {
            // 1. Obtenemos datos (Ya filtrados por ROLE_FUNDACION en el Service)
            List<Map<String, Object>> datosFundaciones = analyticsService.obtenerDatosParaPython();

            // 2. Llamada a Python (Usamos 127.0.0.1 que es más directo)
            String pythonUrl = "http://127.0.0.1:5000/api/payments/analytics/report";
            RestTemplate restTemplate = new RestTemplate();
            
            // 3. Petición POST a Python
            Map<String, Object> respuestaPython = restTemplate.postForObject(pythonUrl, datosFundaciones, Map.class);

            if (respuestaPython != null) {
                model.addAttribute("resumen", respuestaPython.get("resumen_general"));
                model.addAttribute("categorias", respuestaPython.get("categorias"));
                model.addAttribute("riesgo", respuestaPython.get("fundaciones_riesgo"));
            }

        } catch (Exception e) {
            // Si Python falla, enviamos datos en 0 para que el HTML cargue sin error
            Map<String, Object> resumenVacio = new HashMap<>();
            resumenVacio.put("total_recaudado_global", 0);
            resumenVacio.put("total_visitas_global", 0);
            resumenVacio.put("promedio_donacion", 0);
            
            model.addAttribute("resumen", resumenVacio);
            model.addAttribute("categorias", new HashMap<String, Integer>());
            model.addAttribute("riesgo", new ArrayList<>());
            model.addAttribute("error", "Nota: El motor de análisis de Python no está respondiendo.");
        }
        return "admin/dashboard-impacto";
    }
}