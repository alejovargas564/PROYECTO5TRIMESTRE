package com.example.Sapib.model;

import jakarta.persistence.*;
import jakarta.persistence.ForeignKey; 
import jakarta.persistence.ConstraintMode; 
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "visita")
public class Visita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Voluntario: Usa el objeto Usuario. Hibernate usará id_voluntario en la DB.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "id_voluntario", // <-- Nombre de columna que Hibernate NECESITA
        nullable = false,
        foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT) // IGNORA LA FK EN MYSQL
    )
    private Usuario voluntario; // Objeto de tipo Usuario

    // Fundación: Usa el objeto Usuario. Hibernate usará id_fundacion en la DB.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "id_fundacion", // <-- Nombre de columna que Hibernate NECESITA
        nullable = false,
        foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT) // IGNORA LA FK EN MYSQL
    )
    private Usuario fundacion; // Objeto de tipo Usuario

    @Column(name = "fecha_visita", nullable = false)
    private LocalDateTime fechaVisita;

    // Estado: PENDIENTE, ACEPTADA, RECHAZADA
    @Column(name = "estado_visita", nullable = false)
    private String estadoVisita = "PENDIENTE";

    @Column(name = "descripcion_voluntario", length = 500)
    private String descripcion;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    // Constructor para agendar
    public Visita(Usuario voluntario, Usuario fundacion, LocalDateTime fechaVisita, String descripcion) {
        this.voluntario = voluntario;
        this.fundacion = fundacion;
        this.fechaVisita = fechaVisita;
        this.descripcion = descripcion;
        this.fechaCreacion = LocalDateTime.now();
    }
}