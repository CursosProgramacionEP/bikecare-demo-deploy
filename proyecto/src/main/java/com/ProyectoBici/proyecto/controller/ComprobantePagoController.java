package com.ProyectoBici.proyecto.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.ProyectoBici.proyecto.dto.ComprobantePago;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.service.ComprobantePagoService;
import com.ProyectoBici.proyecto.shared.exception.UnauthorizedException;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

/**
 * Muestra el comprobante de pago de un método de pago registrado por el usuario.
 */
@Controller
@RequestMapping("/welcome/pagos/comprobante")
@RequiredArgsConstructor
public class ComprobantePagoController {

    private final ComprobantePagoService comprobanteService;

    @GetMapping("/{id}")
    public String ver(@PathVariable("id") int id, HttpSession session, Model model) {
        Usuario usuario = usuarioEnSesion(session);
        ComprobantePago comprobante = comprobanteService.obtener(id, usuario.getId_usuario());

        model.addAttribute("usuario", usuario);
        model.addAttribute("comprobante", comprobante);
        return "comprobante-pago";
    }

    private Usuario usuarioEnSesion(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            throw new UnauthorizedException();
        }
        return usuario;
    }
}
