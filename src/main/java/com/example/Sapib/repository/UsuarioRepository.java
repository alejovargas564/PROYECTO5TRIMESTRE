package com.example.Sapib.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.Sapib.model.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    // Buscar usuario por nombre de usuario
    Optional<Usuario> findByUserName(String userName);

    // Validar si existe un username
    boolean existsByUserName(String userName);

    // Buscador general por campos
    @Query("SELECT u FROM Usuario u WHERE " +
           "LOWER(u.userName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.correoUsuario) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.telefonoUsuario) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.rol) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Usuario> buscar(@Param("keyword") String keyword);
}
