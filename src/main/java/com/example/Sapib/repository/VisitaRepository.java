package com.example.Sapib.repository;

import com.example.Sapib.model.Visita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface VisitaRepository extends JpaRepository<Visita, Integer> {

    // --- MÉTODOS QUE YA TENÍAS (Mantenidos) ---
    
    // Buscar por ID del objeto Voluntario
    List<Visita> findByVoluntario_IdOrderByFechaVisitaDesc(Integer voluntarioId);

    // Buscar por ID del objeto Fundación (todas)
    List<Visita> findByFundacion_IdOrderByFechaVisitaDesc(Integer fundacionId);

    // Buscar por ID de Fundación y estado PENDIENTE
    List<Visita> findByFundacion_IdAndEstadoVisita(Integer fundacionId, String estadoVisita);


    // --- MÉTODOS NUEVOS PARA EL DASHBOARD DE PYTHON ---

    /**
     * Cuenta cuántas visitas tiene una fundación específica.
     * Este es el que pedía el AnalyticsService para medir el impacto.
     */
    long countByFundacion_Id(Integer fundacionId);

    /**
     * Busca todas las visitas de una fundación (sin orden)
     */
    List<Visita> findByFundacion_Id(Integer fundacionId);
}