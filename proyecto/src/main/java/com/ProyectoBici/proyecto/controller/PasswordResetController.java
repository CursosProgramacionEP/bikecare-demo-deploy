package com.ProyectoBici.proyecto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ProyectoBici.proyecto.service.PasswordResetService;
import com.ProyectoBici.proyecto.shared.exception.BusinessException;
import com.ProyectoBici.proyecto.shared.exception.ResourceNotFoundException;
import com.ProyectoBici.proyecto.shared.message.FlashMessages;

import lombok.RequiredArgsConstructor;

/**
 * Recuperación de contraseña (HU-17).
 */
@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    @GetMapping("/forgot-password")
    public String mostrarFormularioOlvido() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String solicitarRecuperacion(@RequestParam("correo") String correo, RedirectAttributes redirect) {
        passwordResetService.solicitar(correo);
        FlashMessages.success(redirect, "Si el correo existe, recibirás un enlace de recuperación.");
        return "redirect:/login";
    }

    @GetMapping("/reset-password")
    public String mostrarFormularioReset(@RequestParam("token") String token, Model model, RedirectAttributes redirect) {
        try {
            passwordResetService.validar(token);
        } catch (ResourceNotFoundException e) {
            FlashMessages.error(redirect, e.getMessage());
            return "redirect:/forgot-password";
        }
        model.addAttribute("token", token);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String restablecer(@RequestParam("token") String token,
            @RequestParam("contrasenia") String contrasenia,
            @RequestParam("confirmar") String confirmar,
            RedirectAttributes redirect) {
        if (contrasenia == null || !contrasenia.equals(confirmar)) {
            FlashMessages.error(redirect, "Las contraseñas no coinciden.");
            return "redirect:/reset-password?token=" + token;
        }
        try {
            passwordResetService.restablecer(token, contrasenia);
        } catch (BusinessException e) {
            FlashMessages.error(redirect, e.getMessage());
            return "redirect:/reset-password?token=" + token;
        }
        FlashMessages.success(redirect, "Contraseña restablecida. Ya puedes iniciar sesión.");
        return "redirect:/login";
    }
}
