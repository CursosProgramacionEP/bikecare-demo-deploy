package com.ProyectoBici.proyecto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ProyectoBici.proyecto.entity.Usuario;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/welcome")
public class WelcomeController {

    @GetMapping
    public String welcomePrincipal(HttpSession session, Model model) {
        // 1) Leer el usuario guardado en sesión tras el login (AuthController lo puso ahí).
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }
        // 2) Publicarlo al HTML: el topbar de welcome.html lee ${usuario.nombre}.
        model.addAttribute("usuario", usuario);
        return "welcome";
    }

    @GetMapping("/{modulo}")
    public String mostrarModulo(
            @PathVariable("modulo") String modulo,
            HttpSession session,
            Model model) {

        // Validar que el usuario esté autenticado
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        // "usuario" alimenta el topbar (${usuario.nombre}); "moduloActivo" decide qué fragmento central se muestra.
        model.addAttribute("usuario", usuario);
        model.addAttribute("moduloActivo", modulo); // Pasamos 'panelControl', 'garaje', etc.

        return "welcome";
    }
}