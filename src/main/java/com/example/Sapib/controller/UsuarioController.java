package com.example.Sapib.controller;

import com.example.Sapib.model.Usuario;
import com.example.Sapib.service.UsuarioService;
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

    // ===========================
    // LOGIN
    // ===========================
    @GetMapping("/login")
    public String login() {
        return "login";
    }

    // ===========================
    // REGISTRO
    // ===========================
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registro";
    }

    @PostMapping("/registro")
    public String procesarRegistro(@ModelAttribute Usuario usuario) {

        if (!"VOLUNTARIO".equals(usuario.getRol()) && !"FUNDACION".equals(usuario.getRol())) {
            return "redirect:/registro?error=rol_invalido";
        }

        usuario.setRol("ROLE_" + usuario.getRol());
        usuarioService.crearUsuario(usuario);

        return "redirect:/login?registroExitoso";
    }

    // ===========================
    // LISTADO ADMIN
    // ===========================
    @GetMapping("/usuarios")
    public String listarUsuarios(
            @RequestParam(required = false) String nombre,
            Model model) {

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

    // ==========================================================
    // PERFIL DEL USUARIO (VOLUNTARIO + FUNDACIÓN + ADMIN)
    // ==========================================================
    @GetMapping({"/perfil", "/voluntario/perfil", "/fundacion/perfil"})
    public String perfil(Model model, Authentication auth) {

        String userName = auth.getName();

        Usuario usuario = usuarioService.buscarPorUserName(userName)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());
        String rolActual =
                roles.contains("ROLE_ADMIN") ? "ADMIN" :
                roles.contains("ROLE_FUNDACION") ? "FUNDACION" :
                "VOLUNTARIO";

        model.addAttribute("usuario", usuario);
        model.addAttribute("rolActual", rolActual);

        return "perfil"; // ⬅️ Asegúrate de tener plantilla perfil.html
    }

    // ==========================================================
    // GUARDAR CAMBIOS DEL PERFIL (TELÉFONO + CONTRASEÑA)
    // ==========================================================
    @PostMapping({"/perfil/guardar", "/voluntario/perfil/guardar", "/fundacion/perfil/guardar"})
    public String guardarPerfil(@ModelAttribute("usuario") Usuario form, Authentication auth) {

        String userName = auth.getName();

        Usuario actual = usuarioService.buscarPorUserName(userName)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // ========= CAMPOS PERMITIDOS =========
        actual.setTelefonoUsuario(form.getTelefonoUsuario());

        // ========= CONTRASEÑA =========
        if (form.getPassword() != null && !form.getPassword().trim().isEmpty()) {
            actual.setPassword(usuarioService.encriptarPassword(form.getPassword()));
        }

        usuarioService.actualizarDatosPerfil(actual);

        // ========= REDIRECCIÓN POR ROL =========
        Set<String> roles = AuthorityUtils.authorityListToSet(auth.getAuthorities());

        if (roles.contains("ROLE_ADMIN")) {
            return "redirect:/usuarios?perfilActualizado";
        }

        if (roles.contains("ROLE_FUNDACION")) {
            return "redirect:/fundacion/dashboard?perfilActualizado";
        }

        return "redirect:/voluntario/dashboard?perfilActualizado";
    }

    // ===========================
    // REDIRECCIÓN POST LOGIN
    // ===========================
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
