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
    public UserDetails loadUserByUsername(String loginUsername) throws UsernameNotFoundException {

        // Buscar por loginUsername (nuevo campo)
        Usuario usuario = usuarioRepository.findByUserName(loginUsername)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuario no encontrado: " + loginUsername)
                );

        // Si el usuario NO está activo / aprobado
        if (!usuario.isEstadoUsuario()) {
            throw new UsernameNotFoundException(
                    "Tu cuenta aún no ha sido aprobada por un administrador."
            );
        }

        // Spring Security requiere el prefijo "ROLE_"
        String rolSpring = usuario.getRol().startsWith("ROLE_")
                ? usuario.getRol()
                : "ROLE_" + usuario.getRol();

        return new User(
                usuario.getUserName(),  // ⬅️ Ahora el login correcto
                usuario.getPassword(),
                Collections.singletonList(new SimpleGrantedAuthority(rolSpring))
        );
    }
}
