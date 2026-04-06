package com.example.Sapib.security;

import com.example.Sapib.model.Usuario;
import com.example.Sapib.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String loginInput) throws UsernameNotFoundException {

        // CAMBIO CLAVE: Buscamos por Correo O Documento usando el valor que viene del login
        Usuario usuario = usuarioRepository.findByCorreoUsuarioOrNumeroDocumento(loginInput, loginInput)
                .orElseThrow(() ->
                        new UsernameNotFoundException("No se encontró usuario con: " + loginInput)
                );

        // Mantenemos tu validación de cuenta activa
        if (!usuario.isEstadoUsuario()) {
            throw new UsernameNotFoundException(
                    "Tu cuenta aún no ha sido aprobada por un administrador."
            );
        }

        // Aseguramos el prefijo ROLE_ para Spring Security
        String rolSpring = usuario.getRol().startsWith("ROLE_")
                ? usuario.getRol()
                : "ROLE_" + usuario.getRol();

        return new User(
                usuario.getCorreoUsuario(), // Usamos el correo como identificador de la sesión
                usuario.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(rolSpring))
        );
    }
}