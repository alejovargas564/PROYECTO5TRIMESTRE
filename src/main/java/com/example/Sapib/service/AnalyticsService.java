package com.example.Sapib.service;

import com.example.Sapib.model.Usuario;
import com.example.Sapib.model.Visita;
import com.example.Sapib.repository.UsuarioRepository;
import com.example.Sapib.repository.VisitaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class AnalyticsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private VisitaRepository visitaRepository;

    // --- MÉTODO PARA LA FUNDACIÓN (Reportes Individuales) ---
    public Map<String, Object> prepararDatosFundacion(Integer fundacionId) {
        Map<String, Object> payload = new HashMap<>();
        Random random = new Random();
        
        List<Visita> visitas = visitaRepository.findByFundacion_Id(fundacionId);
        long aprobadas = visitas.stream().filter(v -> "ACEPTADA".equals(v.getEstadoVisita())).count();
        long pendientes = visitas.stream().filter(v -> "PENDIENTE".equals(v.getEstadoVisita())).count();

        List<Map<String, Object>> visitasData = visitas.stream().map(v -> {
            Map<String, Object> m = new HashMap<>();
            m.put("nombreVoluntario", v.getVoluntario().getUserName());
            m.put("estado", v.getEstadoVisita());
            return m;
        }).toList();

        List<Map<String, Object>> donacionesData = new ArrayList<>();
        String[] nombres = {"Juan Perez", "Maria Lopez", "Anonimo", "Empresa X"};
        for (int i = 0; i < 5; i++) {
            Map<String, Object> d = new HashMap<>();
            d.put("nombreDonante", nombres[random.nextInt(nombres.length)]);
            d.put("monto", 50000 + random.nextInt(150000));
            donacionesData.add(d);
        }

        payload.put("visitas", visitasData);
        payload.put("donaciones", donacionesData);
        payload.put("conteoAprobadas", aprobadas);
        payload.put("conteoPendientes", pendientes);
        
        return payload;
    }

    // --- MÉTODO PARA EL ADMIN (Dashboard Global - EL QUE FALTABA) ---
    public List<Map<String, Object>> obtenerDatosParaPython() {
        List<Usuario> todos = usuarioRepository.findAll(); 
        List<Map<String, Object>> listaDatos = new ArrayList<>();
        Random random = new Random();

        for (Usuario u : todos) {
            if ("ROLE_FUNDACION".equals(u.getRol())) {
                Map<String, Object> data = new HashMap<>();
                data.put("nombre", u.getUserName()); 
                data.put("categoria", u.getSector() != null ? u.getSector() : "Social"); 
                data.put("visitas", visitaRepository.countByFundacion_Id(u.getId()));
                data.put("recaudado", 100000 + random.nextInt(900000)); // Simulación para Admin
                listaDatos.add(data);
            }
        }
        return listaDatos;
    }
}