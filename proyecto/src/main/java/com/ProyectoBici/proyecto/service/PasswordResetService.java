package com.ProyectoBici.proyecto.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.repository.UsuarioRepository;
import com.ProyectoBici.proyecto.shared.exception.BusinessException;
import com.ProyectoBici.proyecto.shared.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * Recuperación de contraseña (HU-17).
 * Genera un token de un solo uso con expiración de 30 minutos y lo envía por correo.
 */
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int MINUTOS_EXPIRACION = 30;
    private static final int MIN_PASSWORD = 8;

    private final UsuarioRepository usuarioRepository;
    private final ObjectProvider<JavaMailSender> mailSenderProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${spring.mail.username:}")
    private String mailFrom;

    /**
     * Solicita la recuperación. No revela si el correo existe.
     */
    @Transactional
    public void solicitar(String correo) {
        if (correo == null || correo.isBlank()) {
            return;
        }
        usuarioRepository.findByCorreo(correo.trim()).ifPresent(usuario -> {
            String token = UUID.randomUUID().toString();
            usuario.setResetToken(token);
            usuario.setResetTokenExpiry(LocalDateTime.now().plusMinutes(MINUTOS_EXPIRACION));
            usuarioRepository.save(usuario);

            String enlace = baseUrl + "/reset-password?token=" + token;
            enviarCorreo(usuario.getCorreo(), enlace);
        });
    }

    /**
     * Valida el token y devuelve el usuario asociado.
     */
    @Transactional(readOnly = true)
    public Usuario validar(String token) {
        if (token == null || token.isBlank()) {
            throw new ResourceNotFoundException("El enlace no es válido.", "/forgot-password");
        }
        Usuario usuario = usuarioRepository.findByResetToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("El enlace no es válido o ya fue usado.", "/forgot-password"));

        if (usuario.getResetTokenExpiry() == null || usuario.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new ResourceNotFoundException("El enlace expiró. Solicita uno nuevo.", "/forgot-password");
        }
        return usuario;
    }

    /**
     * Restablece la contraseña y limpia el token.
     */
    @Transactional
    public void restablecer(String token, String nuevaClave) {
        Usuario usuario = validar(token);
        if (nuevaClave == null || nuevaClave.length() < MIN_PASSWORD) {
            throw new BusinessException("La contraseña debe tener al menos " + MIN_PASSWORD + " caracteres.");
        }
        usuario.setContrasenia(passwordEncoder.encode(nuevaClave));
        usuario.setResetToken(null);
        usuario.setResetTokenExpiry(null);
        usuarioRepository.save(usuario);
    }

    private void enviarCorreo(String destinatario, String enlace) {
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            log.warn("SMTP no configurado. Enlace de recuperación para {}: {}", destinatario, enlace);
            return;
        }
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(mailFrom.isBlank() ? "noreply@bikecare.com" : mailFrom);
            mensaje.setTo(destinatario);
            mensaje.setSubject("Recuperación de contraseña - BikeCare");
            mensaje.setText("Hola,\n\nPara restablecer tu contraseña, abre este enlace (válido por "
                    + MINUTOS_EXPIRACION + " minutos):\n" + enlace
                    + "\n\nSi no solicitaste este cambio, ignora este correo.");
            mailSender.send(mensaje);
        } catch (Exception e) {
            log.warn("No se pudo enviar el correo a {}. Enlace de recuperación: {}", destinatario, enlace);
        }
    }
}
