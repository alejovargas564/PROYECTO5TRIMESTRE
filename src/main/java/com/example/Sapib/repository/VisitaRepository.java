package com.example.Sapib.repository;

import com.example.Sapib.model.Visita;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface VisitaRepository extends JpaRepository<Visita, Integer> {

    // Buscar por ID del objeto Voluntario
    List<Visita> findByVoluntario_IdOrderByFechaVisitaDesc(Integer voluntarioId);

    // Buscar por ID del objeto Fundación (todas)
    List<Visita> findByFundacion_IdOrderByFechaVisitaDesc(Integer fundacionId);

    // Buscar por ID de Fundación y estado PENDIENTE
    List<Visita> findByFundacion_IdAndEstadoVisita(Integer fundacionId, String estadoVisita);
}