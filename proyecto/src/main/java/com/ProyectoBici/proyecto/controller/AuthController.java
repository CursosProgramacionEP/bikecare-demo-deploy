package com.ProyectoBici.proyecto.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.service.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * indexConotroller
 * 
 */
@Controller 
public class AuthController {

    // LOGIN

    // Crea la variable UsuarioService sin necesidad de poner --> new() ..
    @Autowired 
    private UsuarioService usuarioService;

    // Entras pa logearte
    @GetMapping ("/login")
    public String mostrarLogin() {
        return "index";
    }

    // En el login hay un formulario, ese form tiene action="login", al colocar eso
    // los datos van a llegar a esta funcion sin problem y ns que mas xd
    @PostMapping ("/login")
    public String procesarLogin(
            @RequestParam ("correo") String correo,
            @RequestParam("contrasenia") String contrasenia,
            HttpSession session,
            HttpServletRequest request) {

        if (usuarioService.estaBloqueado(correo)) {
            return "redirect:/login?bloqueado";
        }

        Usuario usuario = usuarioService.autenticar(correo, contrasenia);
        if (usuario != null) {
            usuarioService.limpiarIntentos(usuario);
            request.changeSessionId();
            session.setAttribute("usuarioLogueado", usuario);
            return UsuarioService.esAdminUnico(usuario) ? "redirect:/admin/pagos" : "redirect:/welcome";
        } else {
            usuarioService.registrarFallo(correo);
            return "redirect:/login?error";
        }
    }

    // LOGOUT: solo por POST, junto con el token CSRF del formulario
    @PostMapping("/logout")
    public String cerrarSesion(HttpSession session) {
        session.invalidate();
        return "redirect:/login";
    }

    //Entras al formulario de registro
    @GetMapping("/registro")
    public String mostrarRegistro() {
        return "registro";
    }

    // REGISTRO

    @PostMapping("/registrar")
    public String procesarRegistro(
            @RequestParam("nombre") String nombre,
            @RequestParam("apellidos") String apellidos,
            @RequestParam("correo") String correo,
            @RequestParam("contrasenia") String contrasenia) {
                
        usuarioService.guardarUsuario(nombre, apellidos, correo, contrasenia);
        return "redirect:/login";
    }

}