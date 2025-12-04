package com.example.Sapib.config;

import com.example.Sapib.model.Usuario;
import com.example.Sapib.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(UsuarioRepository repo) {
        return args -> {

            // Verifica si ya existe un usuario admin
            if (repo.findByUserName("admin").isEmpty()) {

                Usuario admin = new Usuario();

                // CAMBIO: userName → loginUsername
                admin.setUserName("admin");

                admin.setPassword(new BCryptPasswordEncoder().encode("123"));
                admin.setRol("ADMIN");

                admin.setCorreoUsuario("admin@sapib.com");
                admin.setTelefonoUsuario("0000000000");
                admin.setTipoDocumento("CC");
                admin.setNumeroDocumento("999999999");
                admin.setEstadoUsuario(true);

                admin.setCreateAt(LocalDateTime.now());

                repo.save(admin);

                System.out.println("✅ Usuario ADMIN creado con éxito");
            } else {
                System.out.println("ℹ️ Usuario ADMIN ya existe");
            }

        };
    }
}
