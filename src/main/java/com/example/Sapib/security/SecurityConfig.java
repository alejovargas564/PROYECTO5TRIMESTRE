package com.example.Sapib.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collection;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                // 1. RUTAS ESTÁTICAS
                .requestMatchers("/css/**", "/index","/conocenos", "/js/**", "/img/**").permitAll()

                // 2. RUTAS PÚBLICAS
                .requestMatchers("/", "/login", "/registro").permitAll()

                // 3. RUTAS ADMIN
                .requestMatchers("/usuarios/**").hasRole("ADMIN")

                // 3.1 RUTAS DE PERFIL (TODOS LOS ROLES PUEDEN)
                .requestMatchers("/perfil", "/perfil/**")
                    .hasAnyRole("ADMIN", "FUNDACION", "VOLUNTARIO")

                // 4. RUTAS POR ROL
                .requestMatchers("/fundacion/**").hasRole("FUNDACION")
                .requestMatchers("/voluntario/**").hasRole("VOLUNTARIO")

                // 5. CUALQUIER OTRA RUTA
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(customAuthenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            )
            .csrf(csrf -> csrf.disable()); // TEMPORAL PARA TESTING

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request,
                                               HttpServletResponse response,
                                               Authentication authentication) throws IOException, ServletException {

                Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
                String redirectUrl = "/home";

                for (GrantedAuthority authority : authorities) {
                    String role = authority.getAuthority();

                    if (role.equals("ROLE_ADMIN")) {
                        redirectUrl = "/admin/home";
                        break;
                    } else if (role.equals("ROLE_FUNDACION")) {
                        redirectUrl = "/fundacion/dashboard";
                        break;
                    } else if (role.equals("ROLE_VOLUNTARIO")) {
                        redirectUrl = "/voluntario/dashboard";
                        break;
                    }
                }

                response.sendRedirect(redirectUrl);
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
