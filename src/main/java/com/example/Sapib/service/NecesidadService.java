package com.example.Sapib.service;

import com.example.Sapib.model.Necesidad;
import com.example.Sapib.model.Usuario;
import com.example.Sapib.repository.NecesidadRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors; // Importación necesaria

@Service
public class NecesidadService {

    @Autowired
    private NecesidadRepository necesidadRepository;
    
    @Autowired
    private UsuarioService usuarioService; // Para obtener el objeto Usuario

    // ========================================================
    // CRUD
    // ========================================================
    @Transactional
    public Necesidad guardarNecesidad(Necesidad necesidad, Integer fundacionId) {
        // Buscamos la Fundación por ID para asegurar la relación
        Usuario fundacion = usuarioService.buscarPorId(fundacionId)
                .orElseThrow(() -> new RuntimeException("Fundación no encontrada"));

        necesidad.setFundacion(fundacion);
        
        // Si es una nueva necesidad, establecer la fecha de publicación
        if (necesidad.getId() == null) {
            necesidad.setFechaPublicacion(LocalDateTime.now());
        }

        return necesidadRepository.save(necesidad);
    }

    public Optional<Necesidad> buscarPorId(Integer id) {
        return necesidadRepository.findById(id);
    }
    
    // Método utilizado por FundacionController (Mantenido por compatibilidad)
    public void eliminarNecesidad(Integer id) {
        if (!necesidadRepository.existsById(id)) {
             throw new RuntimeException("La necesidad con ID " + id + " no existe.");
        }
        necesidadRepository.deleteById(id);
    }
    
    /**
     * NUEVO: Implementación del método que AdminController llama para eliminar.
     */
    public void deleteNecesidadById(Integer id) {
        // Reutilizamos la lógica del método anterior
        eliminarNecesidad(id);
    }

    // ========================================================
    // LISTADOS Y FILTROS
    // ========================================================

    /**
     * NUEVO: Implementación del método que AdminController llama para listar todo.
     */
    public List<Necesidad> findAllNecesidades() {
        return necesidadRepository.findAll();
    }
    
    // Listar necesidades de una Fundación específica (Base para filtrado)
    public List<Necesidad> listarPorFundacion(Integer fundacionId) {
        return necesidadRepository.findByFundacion_Id(fundacionId);
    }
    
    /**
     * NUEVO: Filtra las necesidades de una fundación por título/descripción y tipo.
     */
    public List<Necesidad> filtrarNecesidadesFundacion(Integer fundacionId, String titulo, String tipo) {
        // 1. Obtener la lista base (todas las necesidades de la fundación)
        List<Necesidad> lista = listarPorFundacion(fundacionId);

        // 2. Aplicar filtro por título/descripción (búsqueda por keyword)
        if (titulo != null && !titulo.trim().isEmpty()) {
            String keyword = titulo.toLowerCase().trim();
            lista = lista.stream()
                    .filter(n -> (n.getTitulo() != null && n.getTitulo().toLowerCase().contains(keyword)) ||
                                 (n.getDescripcion() != null && n.getDescripcion().toLowerCase().contains(keyword)))
                    .collect(Collectors.toList());
        }
        
        // 3. Aplicar filtro por tipo de necesidad
        if (tipo != null && !tipo.trim().isEmpty()) {
            String tipoFiltro = tipo.toUpperCase().trim();
            lista = lista.stream()
                    .filter(n -> n.getTipoNecesidad() != null && n.getTipoNecesidad().equals(tipoFiltro))
                    .collect(Collectors.toList());
        }

        return lista;
    }

    // Listar todas las necesidades activas (para Voluntarios)
    public List<Necesidad> listarActivas() {
        return necesidadRepository.findByEstadoActivo(true);
    }
}