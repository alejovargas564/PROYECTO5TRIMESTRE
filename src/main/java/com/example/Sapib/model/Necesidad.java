package com.example.Sapib.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "necesidad")
public class Necesidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String titulo;

    @Column(length = 1000)
    private String descripcion;

    @Column(name = "tipo_necesidad") 
    private String tipoNecesidad;
    
    @Column(nullable = false)
    private boolean estadoActivo = true;

    // NUEVO CAMPO: Nivel de Prioridad (BAJA, MEDIA, ALTA)
    @Column(nullable = false)
    private String prioridad = "BAJA";

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fundacion", nullable = false)
    private Usuario fundacion;

    @Column(name = "fecha_publicacion", nullable = false, updatable = false)
    private LocalDateTime fechaPublicacion;

    @PrePersist
    protected void onCreate() {
        this.fechaPublicacion = LocalDateTime.now();
    }
}