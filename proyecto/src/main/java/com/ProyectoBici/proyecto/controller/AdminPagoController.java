package com.ProyectoBici.proyecto.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.service.AdminPagoService;
import com.ProyectoBici.proyecto.service.UsuarioService;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/admin")
public class AdminPagoController {

    @Autowired
    private AdminPagoService adminPagoService;

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping("/pagos")
    public String verPagosPendientes(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
        try {
            Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");

            if (usuarioLogueado == null || !usuarioService.esAdminUnico(usuarioLogueado)) {
                redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
                return "redirect:/";
            }

            model.addAttribute("pagosPendientes", adminPagoService.obtenerPagosPendientes());
            return "admin-pagos";
        } catch (Exception e) {
            e.printStackTrace(); // Esto imprimirá el error real en tu terminal de VS Code
            redirectAttributes.addFlashAttribute("error", "Error interno: " + e.getMessage());
            return "redirect:/";
        }
    }
    @PostMapping("/pagos/aprobar/{id}")
    public String aprobarPago(@PathVariable("id") int idMetodoPago, HttpSession session, RedirectAttributes redirectAttributes) {
        Usuario usuarioLogueado = (Usuario) session.getAttribute("usuarioLogueado");

        if (usuarioLogueado == null || !usuarioService.esAdminUnico(usuarioLogueado)) {
            redirectAttributes.addFlashAttribute("error", "Acceso no autorizado.");
            return "redirect:/";
        }

        try {
            adminPagoService.aprobarPago(idMetodoPago);
            redirectAttributes.addFlashAttribute("exito", "Pago aprobado con éxito. Usuario actualizado a Premium.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error al aprobar el pago: " + e.getMessage());
        }

        return "redirect:/admin/pagos";
    }
}