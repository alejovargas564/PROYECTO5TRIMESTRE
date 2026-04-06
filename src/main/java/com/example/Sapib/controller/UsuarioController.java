package com.example.Sapib.controller;

import com.example.Sapib.model.Usuario;
import com.example.Sapib.service.UsuarioService;
import com.example.Sapib.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Controller
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario obtenerUsuarioLogueado(Authentication auth) {
        String loginInfo = auth.getName();
        return usuarioRepository.findByCorreoUsuarioOrNumeroDocumento(loginInfo, loginInfo)
                .or(() -> usuarioRepository.findByUserName(loginInfo))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute Usuario usuario, Model model) {
        // EQUIVALENTE A TRY:
        try {
            // 1. Validar Roles permitidos
            if (!"VOLUNTARIO".equals(usuario.getRol()) && !"FUNDACION".equals(usuario.getRol())) {
                throw new IllegalArgumentException("Rol no válido seleccionado.");
            }

            // 2. Validar que el Teléfono sea solo números
            if (usuario.getTelefonoUsuario() != null && !usuario.getTelefonoUsuario().matches("\\d+")) {
                throw new IllegalArgumentException("El teléfono debe contener solo números.");
            }

            // 3. Validar que el Documento sea solo números
            if (usuario.getNumeroDocumento() != null && !usuario.getNumeroDocumento().matches("\\d+")) {
                throw new IllegalArgumentException("El número de documento debe contener solo números.");
            }

            // 4. Validar Correo básico
            if (!usuario.getCorreoUsuario().contains("@")) {
                throw new IllegalArgumentException("Ingrese un correo electrónico válido.");
            }

            // Si todo está bien, procesamos
            usuario.setRol("ROLE_" + usuario.getRol());
            usuarioService.crearUsuario(usuario);
            return "redirect:/login?registroExitoso";

        } 
        // EQUIVALENTE A EXCEPT:
        catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage()); // Pasamos el mensaje al HTML
            model.addAttribute("usuario", usuario);     // Devolvemos los datos para no perder lo escrito
            return "registro";
        } 
        catch (Exception e) {
            model.addAttribute("error", "Error inesperado al procesar el registro.");
            model.addAttribute("usuario", usuario);
            return "registro";
        }
    }

    // --- EL RESTO DE TUS MÉTODOS SE MANTIENEN IGUAL ---

    @GetMapping("/usuarios")
    public String listarUsuarios(@RequestParam(required = false) String nombre, Model model) {
        if (nombre != null && !nombre.trim().isEmpty()) {
            model.addAttribute("usuarios", usuarioService.filtrarUsuarios(nombre));
        } else {
            model.addAttribute("usuarios", usuarioService.listarTodos());
        }
        model.addAttribute("nombre", nombre);
        return "usuarios";
    }

    @GetMapping("/usuarios/nuevo")
    public String nuevoUsuarioAdmin(Model model) {
        model.addAttribute("usuario", new Usuario());
        model.addAttribute("modo", "crear");
        return "form";
    }

    @PostMapping("/usuarios/guardar")
    public String guardarUsuario(@ModelAttribute Usuario usuario) {
        if (usuario.getId() == null || usuario.getId() == 0) {
            usuarioService.crearUsuario(usuario);
        } else {
            usuarioService.actualizarUsuario(usuario.getId(), usuario);
        }
        return "redirect:/usuarios?ok";
    }

    @GetMapping("/usuarios/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        Usuario usuario = usuarioService.buscarPorId(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        model.addAttribute("usuario", usuario);
        model.addAttribute("modo", "editar");
        return "form";
    }

    @GetMapping("/usuarios/eliminar/{id}")
    public String eliminar(@PathVariable Integer id) {
        usuarioService.eliminarUsuario(id);
        return "redirect:/usuarios?eliminado";
    }

    @GetMapping({"/perfil", "/voluntario/perfil", "/fundacion/perfil"})
    public String perfil(Model model, Authentication auth) {
        Usuario usuario = obtenerUsuarioLogueado(auth);
        Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
        String rolActual = roles.contains("ROLE_ADMIN") ? "ADMIN" :
                           roles.contains("ROLE_FUNDACION") ? "FUNDACION" : "VOLUNTARIO";

        model.addAttribute("usuario", usuario);
        model.addAttribute("rolActual", rolActual);
        return "perfil";
    }

    @PostMapping({"/perfil/guardar", "/voluntario/perfil/guardar", "/fundacion/perfil/guardar"})
    public String guardarPerfil(@ModelAttribute("usuario") Usuario form, Authentication auth) {
        Usuario actual = obtenerUsuarioLogueado(auth);
        actual.setTelefonoUsuario(form.getTelefonoUsuario());
        if (form.getPassword() != null && !form.getPassword().trim().isEmpty()) {
            actual.setPassword(usuarioService.encriptarPassword(form.getPassword()));
        }
        usuarioService.actualizarDatosPerfil(actual);
        Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
        if (roles.contains("ROLE_ADMIN")) return "redirect:/usuarios?perfilActualizado";
        if (roles.contains("ROLE_FUNDACION")) return "redirect:/fundacion/dashboard?perfilActualizado";
        return "redirect:/voluntario/dashboard?perfilActualizado";
    }

    @GetMapping("/redirect-by-role")
    public String redirectByRole(Authentication auth) {
        for (GrantedAuthority authority : auth.getAuthorities()) {
            switch (authority.getAuthority()) {
                case "ROLE_ADMIN": return "redirect:/usuarios";
                case "ROLE_FUNDACION": return "redirect:/fundacion/dashboard";
                case "ROLE_VOLUNTARIO": return "redirect:/voluntario/dashboard";
            }
        }
        return "redirect:/home";
    }
}