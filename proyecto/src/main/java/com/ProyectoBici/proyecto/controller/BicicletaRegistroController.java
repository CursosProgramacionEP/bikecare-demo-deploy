package com.ProyectoBici.proyecto.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.ProyectoBici.proyecto.dto.BicicletaForm;
import com.ProyectoBici.proyecto.entity.Bicicleta;
import com.ProyectoBici.proyecto.entity.Usuario;
import com.ProyectoBici.proyecto.service.BicicletaService;
import com.ProyectoBici.proyecto.shared.message.FlashMessages;

import jakarta.servlet.http.HttpSession;
/**
 * Solo el ALTA de bicicletas (formulario + guardado).
 * Cambia únicamente si cambian las reglas de registro
 * (validación, foto, campos obligatorios). No lista ni instala piezas.
 */
@Controller
@RequestMapping("/welcome/garaje/registrarBici")
public class BicicletaRegistroController {

    @Autowired
    private BicicletaService bicicletaService;

    // GET /welcome/garaje/registrarBici — formulario vacío.
    @GetMapping
    public String formularioBici(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("usuario", usuario);
        model.addAttribute("moduloActivo", "registrarBici");
        return "welcome";
    }

    // POST /welcome/garaje/registrarBici — valida y guarda la bici.
    @PostMapping
    public String guardarBici(
            @ModelAttribute BicicletaForm form,
            HttpSession session,
            RedirectAttributes redirect) {

        Usuario usuario = (Usuario) session.getAttribute("usuarioLogueado");
        if (usuario == null) {
            return "redirect:/login";
        }

        try {
            Bicicleta nueva = bicicletaService.registrar(usuario, form);
            FlashMessages.success(redirect, "Bicicleta " + nueva.getMarca() + " " + nueva.getModelo() + " guardada. Ahora puedes añadirle componentes desde su tarjeta.");
            return "redirect:/welcome/garaje";
        } catch (IllegalArgumentException e) {
            FlashMessages.error(redirect, e.getMessage());
            return "redirect:/welcome/garaje/registrarBici";
        }
    }
}

/** prueba de ramas git */