package com.example.Sapib.repository;

import com.example.Sapib.model.Necesidad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NecesidadRepository extends JpaRepository<Necesidad, Integer> {

    // Listar necesidades por Fundación (para el dashboard de la Fundación)
    List<Necesidad> findByFundacion_Id(Integer fundacionId);

    // Listar todas las necesidades activas (para el Voluntario)
    List<Necesidad> findByEstadoActivo(boolean estadoActivo);
}