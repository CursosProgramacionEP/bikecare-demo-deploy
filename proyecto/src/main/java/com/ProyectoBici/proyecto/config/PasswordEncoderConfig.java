package com.ProyectoBici.proyecto.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Codificador de contrasenias de usuario.
 *
 * <p>
 * BCrypt es un hash con salt aleatorio: es irreversible y lento a proposito,
 * asi que la base de datos nunca guarda la contrasenia en texto plano.
 * </p>
 */
@Configuration
public class PasswordEncoderConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
