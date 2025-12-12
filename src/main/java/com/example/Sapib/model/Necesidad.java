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

    @Column(name = "tipo_necesidad") // Ej: Taller, Materiales, Mentoría
    private String tipoNecesidad;
    
    @Column(nullable = false)
    private boolean estadoActivo = true; // Si la necesidad está activa o ya fue cubierta

    // Relación Many-to-One: Una necesidad pertenece a una Fundación (que es un Usuario)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fundacion", nullable = false)
    private Usuario fundacion;

    @Column(name = "fecha_publicacion", nullable = false)
    private LocalDateTime fechaPublicacion = LocalDateTime.now();
}