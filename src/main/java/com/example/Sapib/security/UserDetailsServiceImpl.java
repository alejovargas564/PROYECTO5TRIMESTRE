package com.example.Sapib.security;

import com.example.Sapib.model.Usuario;
import com.example.Sapib.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String loginInput) throws UsernameNotFoundException {
        // Intentamos buscar por Correo/Documento O por UserName
        Usuario usuario = usuarioRepository.findByCorreoUsuarioOrNumeroDocumento(loginInput, loginInput)
                .or(() -> usuarioRepository.findByUserName(loginInput))
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró el usuario con los datos proporcionados: " + loginInput));

        // Validación de estado: El Admin siempre entra, los demás solo si están activos
        // .contains("ADMIN") cubre "ADMIN" y "ROLE_ADMIN"
        if (!usuario.getRol().toUpperCase().contains("ADMIN") && !usuario.isEstadoUsuario()) {
            throw new UsernameNotFoundException("Cuenta pendiente de activación. Por favor, contacta al administrador.");
        }

        // Normalización del Rol para Spring Security (Prefijo ROLE_)
        String rolRaw = usuario.getRol().toUpperCase();
        String rolFinal = rolRaw.startsWith("ROLE_") ? rolRaw : "ROLE_" + rolRaw;

        // Construcción del objeto User para la sesión
        return User.withUsername(usuario.getCorreoUsuario()) 
                .password(usuario.getPassword())
                .authorities(Collections.singletonList(new SimpleGrantedAuthority(rolFinal)))
                .build();
    }
}