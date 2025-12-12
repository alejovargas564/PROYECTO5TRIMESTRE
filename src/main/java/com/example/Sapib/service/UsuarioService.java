package com.example.Sapib.service;

import com.example.Sapib.model.Usuario;
import com.example.Sapib.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ========================================================
    // CREAR USUARIO
    // ========================================================
    public Usuario crearUsuario(Usuario usuario) {

        if (usuarioRepository.existsByUserName(usuario.getUserName())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }

        // Cifrar contraseña al crear
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setCreateAt(LocalDateTime.now());

        return usuarioRepository.save(usuario);
    }

    // Buscar por ID
    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    // Buscar por username
    public Optional<Usuario> buscarPorUserName(String username) {
        return usuarioRepository.findByUserName(username);
    }

    // Listar todos
    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    // ========================================================
    // ACTUALIZAR USUARIO (ADMIN)
    // ========================================================
    public Usuario actualizarUsuario(Integer id, Usuario usuarioActualizado) {

        return usuarioRepository.findById(id).map(usuario -> {

            // Validar si cambiaron username
            if (!usuario.getUserName().equals(usuarioActualizado.getUserName())) {
                if (usuarioRepository.existsByUserName(usuarioActualizado.getUserName())) {
                    throw new RuntimeException("El nombre de usuario ya está en uso");
                }
            }

            // Admin puede actualizar estos campos:
            usuario.setUserName(usuarioActualizado.getUserName());
            usuario.setCorreoUsuario(usuarioActualizado.getCorreoUsuario());
            usuario.setTelefonoUsuario(usuarioActualizado.getTelefonoUsuario());
            usuario.setTipoDocumento(usuarioActualizado.getTipoDocumento());
            usuario.setNumeroDocumento(usuarioActualizado.getNumeroDocumento());
            usuario.setRol(usuarioActualizado.getRol());
            usuario.setEstadoUsuario(usuarioActualizado.isEstadoUsuario());

            // Contraseña solo si envían una nueva
            if (usuarioActualizado.getPassword() != null &&
                    !usuarioActualizado.getPassword().trim().isEmpty()) {

                usuario.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
            }

            return usuarioRepository.save(usuario);

        }).orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    // ========================================================
    // ELIMINAR
    // ========================================================
    public void eliminarUsuario(Integer id) {
        if (!usuarioRepository.existsById(id)) {
            throw new RuntimeException("El usuario no existe");
        }
        usuarioRepository.deleteById(id);
    }

    // ========================================================
    // FILTROS
    // ========================================================
    public List<Usuario> filtrarUsuarios(String nombre, LocalDateTime desde, LocalDateTime hasta) {
        List<Usuario> lista = usuarioRepository.findAll();

        if (nombre != null && !nombre.trim().isEmpty()) {
            lista = lista.stream()
                    .filter(u -> u.getUserName() != null &&
                            u.getUserName().toLowerCase().contains(nombre.toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (desde != null) {
            lista = lista.stream()
                    .filter(u -> u.getCreateAt() != null &&
                            !u.getCreateAt().isBefore(desde))
                    .collect(Collectors.toList());
        }

        if (hasta != null) {
            lista = lista.stream()
                    .filter(u -> u.getCreateAt() != null &&
                            !u.getCreateAt().isAfter(hasta))
                    .collect(Collectors.toList());
        }

        return lista;
    }

    public List<Usuario> filtrarUsuarios(String nombre) {
        return filtrarUsuarios(nombre, null, null);
    }
    
    // ========================================================
    // NUEVO: LISTAR FUNDACIONES PARA VOLUNTARIO
    // ========================================================
    public List<Usuario> listarFundaciones() {
        return usuarioRepository.findByRol("ROLE_FUNDACION");
    }

    // ========================================================
    // PERFIL - CAMPOS PERMITIDOS
    // ========================================================

    public String encriptarPassword(String password) {
        return passwordEncoder.encode(password);
    }

    /**
     * Actualiza SOLO los campos permitidos del perfil:
     * - Teléfono
     * - Contraseña (si vino desde el controller)
     *
     * Nunca toca: username, correo, rol, documentos, estado.
     */
    @Transactional
    public Usuario actualizarDatosPerfil(Usuario usuario) {

        Usuario original = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Solo teléfono
        original.setTelefonoUsuario(usuario.getTelefonoUsuario());

        // Solo contraseña si viene nueva
        if (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty()) {
            original.setPassword(usuario.getPassword()); // ya está encriptada desde controller
        }

        return usuarioRepository.save(original);
    }

}