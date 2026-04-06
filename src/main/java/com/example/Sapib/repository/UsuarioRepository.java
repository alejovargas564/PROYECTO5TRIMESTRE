package com.example.Sapib.repository;

import java.util.Optional;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.Sapib.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Búsquedas individuales para el flujo de autenticación
    Optional<Usuario> findByUserName(String userName);
    Optional<Usuario> findByCorreoUsuario(String correoUsuario);
    Optional<Usuario> findByNumeroDocumento(String numeroDocumento);
    
    // Búsqueda combinada para el login
    Optional<Usuario> findByCorreoUsuarioOrNumeroDocumento(String correo, String documento);

    // Validación de existencia
    boolean existsByUserName(String userName);

    // Buscador general para el panel de Admin
    @Query("SELECT u FROM Usuario u WHERE " +
           "LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.correoUsuario) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.numeroDocumento) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.rol) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Usuario> buscar(@Param("keyword") String keyword);

    List<Usuario> findByRol(String rol);
}