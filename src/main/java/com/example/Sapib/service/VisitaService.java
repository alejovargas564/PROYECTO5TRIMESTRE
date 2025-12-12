package com.example.Sapib.service;

import com.example.Sapib.model.Visita;
import com.example.Sapib.model.Usuario;
import com.example.Sapib.repository.VisitaRepository;
import com.example.Sapib.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors; // Importación necesaria para usar streams

@Service
public class VisitaService {

    @Autowired
    private VisitaRepository visitaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    // ========================================================
    // AGENDAR VISITA (VOLUNTARIO)
    // ========================================================
    @Transactional
    public Visita agendarVisita(Integer voluntarioId, Integer fundacionId, LocalDateTime fechaVisita, String descripcion) {

        Usuario voluntario = usuarioRepository.findById(voluntarioId)
                .orElseThrow(() -> new RuntimeException("Voluntario (Usuario ID: " + voluntarioId + ") no encontrado"));

        Usuario fundacion = usuarioRepository.findById(fundacionId)
                .orElseThrow(() -> new RuntimeException("Fundación (Usuario ID: " + fundacionId + ") no encontrada"));

        // Validamos el ROL aquí (Lógica de negocio)
        if (!"ROLE_VOLUNTARIO".equals(voluntario.getRol())) {
             throw new RuntimeException("El usuario debe ser un Voluntario");
        }
        if (!"ROLE_FUNDACION".equals(fundacion.getRol())) {
            throw new RuntimeException("El usuario de destino debe ser una Fundación");
        }

        // Creamos la entidad usando los objetos Usuario (el ID es manejado por JPA)
        Visita visita = new Visita(voluntario, fundacion, fechaVisita, descripcion);
        visita.setEstadoVisita("PENDIENTE");

        return visitaRepository.save(visita);
    }

    // ========================================================
    // ACTUALIZAR ESTADO (FUNDACIÓN)
    // ========================================================
    @Transactional
    public Visita actualizarEstadoVisita(Integer visitaId, String nuevoEstado) {
        Visita visita = visitaRepository.findById(visitaId)
                .orElseThrow(() -> new RuntimeException("Visita no encontrada"));

        if (!("ACEPTADA".equals(nuevoEstado) || "RECHAZADA".equals(nuevoEstado))) {
            throw new IllegalArgumentException("Estado de visita inválido");
        }

        visita.setEstadoVisita(nuevoEstado);
        return visitaRepository.save(visita);
    }

    // ========================================================
    // LISTADOS Y FILTROS
    // ========================================================

    // Base: obtiene todas las visitas del voluntario (para ser usada internamente)
    private List<Visita> listarHistorialVoluntarioBase(Integer voluntarioId) {
        return visitaRepository.findByVoluntario_IdOrderByFechaVisitaDesc(voluntarioId);
    }
    
    /**
     * Filtra el historial de visitas de un voluntario por nombre de fundación y estado.
     */
    public List<Visita> filtrarHistorialVoluntario(Integer voluntarioId, String fundacion, String estado) {
        
        // 1. Obtener la lista base (todas las visitas del voluntario)
        List<Visita> lista = listarHistorialVoluntarioBase(voluntarioId);
        
        // 2. Aplicar filtro por nombre de Fundación (insensitive case)
        if (fundacion != null && !fundacion.trim().isEmpty()) {
            String keyword = fundacion.toLowerCase().trim();
            lista = lista.stream()
                .filter(v -> v.getFundacion() != null && 
                             v.getFundacion().getUserName() != null &&
                             v.getFundacion().getUserName().toLowerCase().contains(keyword))
                .collect(Collectors.toList());
        }
        
        // 3. Aplicar filtro por Estado (usa mayúsculas, ya que los estados están en mayúsculas)
        if (estado != null && !estado.trim().isEmpty()) {
            String estadoFiltro = estado.toUpperCase().trim();
            lista = lista.stream()
                .filter(v -> v.getEstadoVisita() != null && 
                             v.getEstadoVisita().equals(estadoFiltro))
                .collect(Collectors.toList());
        }
        
        return lista;
    }

    /**
     * Historial para Voluntario (el método original que ahora llama al filtro principal)
     */
    public List<Visita> listarHistorialVoluntario(Integer voluntarioId) {
        // Llama al nuevo método de filtrado sin aplicar filtros iniciales
        return filtrarHistorialVoluntario(voluntarioId, null, null); 
    }

    // Pendientes de la Fundación (busca por el ID del objeto 'fundacion' mapeado)
    public List<Visita> listarVisitasPendientesFundacion(Integer fundacionId) {
        return visitaRepository.findByFundacion_IdAndEstadoVisita(fundacionId, "PENDIENTE");
    }

    // Historial para Fundación (busca por el ID del objeto 'fundacion' mapeado)
    public List<Visita> listarHistorialFundacion(Integer fundacionId) {
        return visitaRepository.findByFundacion_IdOrderByFechaVisitaDesc(fundacionId);
    }

    // Listar todas las visitas (ADMIN)
    public List<Visita> listarTodasVisitas() {
        return visitaRepository.findAll();
    }
}