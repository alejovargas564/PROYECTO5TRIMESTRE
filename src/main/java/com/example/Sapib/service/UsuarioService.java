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
        // 1. Validar si el Nombre de Usuario ya existe
        if (usuarioRepository.existsByUserName(usuario.getUserName())) {
            throw new RuntimeException("El nombre de usuario ya está en uso");
        }

        // 2. NUEVA VALIDACIÓN: Validar si el Correo o Documento ya existen
        // Esto es vital ahora que los usamos para el Login
        if (usuarioRepository.findByCorreoUsuarioOrNumeroDocumento(usuario.getCorreoUsuario(), usuario.getNumeroDocumento()).isPresent()) {
            throw new RuntimeException("El correo electrónico o el número de documento ya están registrados");
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

    // Buscar por username (Se mantiene por compatibilidad)
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

            // Validar si cambiaron username y si el nuevo ya existe
            if (!usuario.getUserName().equals(usuarioActualizado.getUserName())) {
                if (usuarioRepository.existsByUserName(usuarioActualizado.getUserName())) {
                    throw new RuntimeException("El nombre de usuario ya está en uso");
                }
            }

            // Validar si cambiaron correo o documento y si ya existen en otro usuario
            Optional<Usuario> existente = usuarioRepository.findByCorreoUsuarioOrNumeroDocumento(
                    usuarioActualizado.getCorreoUsuario(), usuarioActualizado.getNumeroDocumento());
            
            if (existente.isPresent() && !existente.get().getId().equals(id)) {
                throw new RuntimeException("El correo o documento ya pertenecen a otro usuario");
            }

            // Actualización de campos
            usuario.setUserName(usuarioActualizado.getUserName());
            usuario.setCorreoUsuario(usuarioActualizado.getCorreoUsuario());
            usuario.setTelefonoUsuario(usuarioActualizado.getTelefonoUsuario());
            usuario.setTipoDocumento(usuarioActualizado.getTipoDocumento());
            usuario.setNumeroDocumento(usuarioActualizado.getNumeroDocumento());
            usuario.setRol(usuarioActualizado.getRol());
            usuario.setEstadoUsuario(usuarioActualizado.isEstadoUsuario());
            usuario.setSector(usuarioActualizado.getSector()); // Aseguramos el campo sector

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
    // FILTROS (Optimizado)
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
                    .filter(u -> u.getCreateAt() != null && !u.getCreateAt().isBefore(desde))
                    .collect(Collectors.toList());
        }

        if (hasta != null) {
            lista = lista.stream()
                    .filter(u -> u.getCreateAt() != null && !u.getCreateAt().isAfter(hasta))
                    .collect(Collectors.toList());
        }

        return lista;
    }

    public List<Usuario> filtrarUsuarios(String nombre) {
        return filtrarUsuarios(nombre, null, null);
    }
    
    public List<Usuario> listarFundaciones() {
        return usuarioRepository.findByRol("ROLE_FUNDACION");
    }

    public String encriptarPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Transactional
    public Usuario actualizarDatosPerfil(Usuario usuario) {
        Usuario original = usuarioRepository.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        original.setTelefonoUsuario(usuario.getTelefonoUsuario());

        if (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty()) {
            original.setPassword(usuario.getPassword()); 
        }

        return usuarioRepository.save(original);
    }
}